package com.kalei.pholocation;

import com.kalei.PhotoLocationApplication;
import com.kalei.interfaces.IMailListener;
import com.kalei.managers.PrefManager;
import com.kalei.models.Photo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class GMailSender extends javax.mail.Authenticator {
    private String mailhost = "smtp.mailgun.org";
//    private String mailhost = "smtp.gmail.com";

    private Context context;
    private String user;
    private String password;
    private Session session;
    private String caption;
    private Photo photo;
    public IMailListener mMailListener;

    static {
        Security.addProvider(new com.kalei.pholocation.JSSEProvider());
    }

    public GMailSender(Context context, String user, String password, Photo p, IMailListener listener) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        this.context = context;
        this.user = user;
        this.password = password;
        this.caption = p.getCaption();
        if (caption == null || caption.length() == 0) {
            this.caption = "";
        }
        this.photo = p;
        mMailListener = listener;
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.EnableSSL.enable", "true");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getInstance(props, this);
        _multipart = new MimeMultipart("mixed");
    }

    private Multipart _multipart;

    public void addAttachment(String filename, String body) throws Exception {
        Log.i("pl", "attaching file: " + filename);
        BodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);
        _multipart.addBodyPart(messageBodyPart);

        BodyPart messageBodyPart2 = new MimeBodyPart();
        body = caption + "\n\n" + body;
        messageBodyPart2.setText(body);
//        messageBodyPart2.setText(subject);

        _multipart.addBodyPart(messageBodyPart2);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    static String getEmail(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = getAccount(accountManager);
        if (account == null) {
            return null;
        } else {
            return account.name;
        }
    }

    private static Account getAccount(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = null;
        }
        return account;
    }

    public synchronized void sendMail(String subject, String body, String sender, String recipients, final String filename, final String scaledImage)
            throws Exception {
        try {

            addAttachment(scaledImage, body);
            MimeMessage message = new MimeMessage(session);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
            message.setSender(new InternetAddress("picture@photolocation.com"));
            message.setReplyTo(new javax.mail.Address[]
                    {
                            new javax.mail.internet.InternetAddress(getEmail(context))
                    });

            message.setSubject(caption.length() > 0 ? caption : subject);
            message.setDataHandler(handler);
            message.setContent(_multipart);
            if (recipients.indexOf(',') > 0) {
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            } else {
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
            }

            int picturesSent = PrefManager.getPicturesSent(context);
            if (!PhotoLocationApplication.debug && BuildConfig.MAX_PICTURES_PER_DAY >= picturesSent) {
                PrefManager.setPicturesSent(context, picturesSent + 1);
                Transport.send(message);
                Log.i("pl", "Sending mail");
//                runOnUiThread(new Thread(new Runnable() {
//                    public void run() {
                File f = new File(scaledImage);
                Log.i("pl", "scaled image size: " + f.length());
                f.delete(); //delete old smaller file

                if (mMailListener != null) {
                    mMailListener.onMailSucceeded(scaledImage);
                }
//                    }
//                }));
            } else {
                Log.i("pl", "MAX USAGE OCCURED");
            }
        } catch (FileNotFoundException e) {
            Log.i("pl", "io exception happened: " + e.getMessage());
            if (mMailListener != null) {
                mMailListener.onMailFailed(e, scaledImage);
            }
        } catch (IOException e) {
            Log.i("pl", "io exception happened: " + e.getMessage());
            if (mMailListener != null) {
                mMailListener.onMailFailed(e, scaledImage);
            }
        } catch (final Exception e) {
//            runOnUiThread(new Thread(new Runnable() {
//                public void run() {
//                    if (mMailListener != null) {
            mMailListener.onMailFailed(e, scaledImage);
//        }
//    }
//            }));

            Log.i("pl", "FAILED: " + e.getMessage());
        }
    }

    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public String getContentType() {
            if (type == null) {
                return "application/octet-stream";
            } else {
                return type;
            }
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}