/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.two.factor.auth;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import de.taimos.totp.TOTP;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Scanner;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author jackw
 */
public class Driver {

    public static void main(String[] args) throws WriterException {
        
        String secretKey = "QDWSM3OYBPGTEVSPB5FKVDM3CSNCWHVK";
        String email = "jack@jem.co";
        String companyName = "JEM";
        String barCodeUrl = Driver.getGoogleAuthenticatorBarCode(secretKey, email, companyName);
        
        Driver.createQRCode(barCodeUrl, "C:\\Users\\jackw\\Desktop\\QR\\qr.png", 200, 200);
        
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Hi " + email + " at " + companyName + " enter your 6-digit code to continue: \n");
        String code = scanner.nextLine();
        if (code.equals(getTOTPCode(secretKey))) {
            System.out.println("Logged in successfully");
        } else {
            System.out.println("Invalid 2FA Code");
        }
    }

    /**
     * Generates 20-byte key for Google Authenticator (required for new entry).
     * SIHGL7J3UE5ZNGGBPINBXSZAGWTCH6RY
     * @return
     */
    public static String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    /**
     * Converts secret 20-byte key to hexadecimal and uses TOPT algorithm to
     * turn the hex code into a 6-digit code based on time. So, we use the
     * hexadecimal value of the secret key plus the current time to produce a
     * 6-digit code that can be used to authenticate.
     * @param secretKey
     * @return
     */
    public static String getTOTPCode(String secretKey) {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(secretKey);
        String hexKey = Hex.encodeHexString(bytes);
        return TOTP.getOTP(hexKey);
    }

    /**
     * Returns String used to generate QR code that user can authenticate with.
     * @param secretKey - 20-byte key associated with the account ID in Google
     * Authenticator
     * @param account - user ID for Google Authenticator (usually email address
     * but can be anything)
     * @param issuer - company name
     * @return
     */
    public static String getGoogleAuthenticatorBarCode(String secretKey, String account, String issuer) {
        try {
            return "otpauth://totp/"
                    + URLEncoder.encode(issuer + ":" + account, "UTF-8").replace("+", "%20")
                    + "?secret=" + URLEncoder.encode(secretKey, "UTF-8").replace("+", "%20")
                    + "&issuer=" + URLEncoder.encode(issuer, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Generates QR code from bar code String.
     * @param barCodeData
     * @param filePath
     * @param height
     * @param width
     * @throws WriterException 
     */
    public static void createQRCode(String barCodeData, String filePath, int height, int width) throws WriterException {
        BitMatrix matrix = new MultiFormatWriter().encode(barCodeData, BarcodeFormat.QR_CODE, width, height);
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            MatrixToImageWriter.writeToStream(matrix, "png", out);
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
}
