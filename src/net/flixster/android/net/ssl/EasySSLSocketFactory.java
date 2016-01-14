package net.flixster.android.net.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.wb.nextgen.R;

import com.wb.nextgen.util.utils.ActivityHolder;
import com.wb.nextgen.util.utils.F;
import com.wb.nextgen.util.utils.FlixsterLogger;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;


/**
 * This socket factory will create ssl socket that accepts Flixster certificates signed by AddTrust Root CA
 */
public class EasySSLSocketFactory implements SocketFactory, LayeredSocketFactory {
	
	/**
	 * @see org.apache.http.conn.scheme.SocketFactory#connectSocket(java.net.Socket, java.lang.String, int,
	 *      java.net.InetAddress, int, org.apache.http.params.HttpParams)
	 */
	public Socket connectSocket(Socket sock, String host, int port, InetAddress localAddress, int localPort,
			HttpParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
		int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
		int soTimeout = HttpConnectionParams.getSoTimeout(params);
		InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
		SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock : createSocket());
		
		if ((localAddress != null) || (localPort > 0)) {
			// we need to bind explicitly
			if (localPort < 0) {
				localPort = 0; // indicates "any"
			}
			InetSocketAddress isa = new InetSocketAddress(localAddress, localPort);
			sslsock.bind(isa);
		}
		
		sslsock.connect(remoteAddress, connTimeout);
		sslsock.setSoTimeout(soTimeout);
		return sslsock;
		
	}
	
	/**
	 * @see org.apache.http.conn.scheme.SocketFactory#createSocket()
	 */
	public Socket createSocket() throws IOException {
		return getSSLContext().getSocketFactory().createSocket();
	}
	
	/**
	 * @see org.apache.http.conn.scheme.SocketFactory#isSecure(java.net.Socket)
	 */
	public boolean isSecure(Socket socket) throws IllegalArgumentException {
		return true;
	}
	
	/**
	 * @see org.apache.http.conn.scheme.LayeredSocketFactory#createSocket(java.net.Socket, java.lang.String, int,
	 *      boolean)
	 */
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
			UnknownHostException {
		return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
	}
	
	private SSLContext sslContext;
	
	private SSLContext getSSLContext() throws IOException {
		if (sslContext == null) {
			sslContext = createEasySSLContext();
		}
		return sslContext;
	}
	
	private static SSLContext createEasySSLContext() throws IOException {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
//            context.init(null, new TrustManager[] { new EasyX509TrustManager(getAddTrustRootCert()) }, null);
            context.init(null, new TrustManager[] { new X509TrustManager() {
    			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
    				return new java.security.cert.X509Certificate[] {};
    			}
    			
    			@Override
    			public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
    					throws java.security.cert.CertificateException {
    				
    			}
    			
    			@Override
    			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
    					throws java.security.cert.CertificateException {
    				
    			}
    		} }, null);
            return context;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
	
	private static KeyStore addTrustRoot;
	
	private static KeyStore getAddTrustRootCert() throws KeyStoreException, NoSuchAlgorithmException,
			CertificateException, IOException {
		if (addTrustRoot == null) {
			Context activity = ActivityHolder.instance().getTopLevelActivity();
			
			// http://blog.donnfelker.com/2011/06/13/trusting-android-certificates-part-duex/
			KeyStore trusted = KeyStore.getInstance("BKS");
			InputStream in = activity.getResources().openRawResource(R.raw.addtrust_external_ca_root);
			try {
				trusted.load(in, "flix208utah".toCharArray());
				FlixsterLogger.d(F.TAG, "EasySSLSocketFactory.getAddTrustRootCert: " + trusted.size());
			} finally {
				in.close();
			}
			addTrustRoot = trusted;
		}
		return addTrustRoot;
	}
	
	// -------------------------------------------------------------------
	// javadoc in org.apache.http.conn.scheme.SocketFactory says :
	// Both Object.equals() and Object.hashCode() must be overridden
	// for the correct operation of some connection managers
	// -------------------------------------------------------------------
	
	public boolean equals(Object obj) {
		return ((obj != null) && obj.getClass().equals(EasySSLSocketFactory.class));
	}
	
	public int hashCode() {
		return EasySSLSocketFactory.class.hashCode();
	}
	
}