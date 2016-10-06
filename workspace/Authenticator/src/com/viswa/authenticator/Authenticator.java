package com.viswa.authenticator;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.google.android.apps.authenticator.Base32String;
import com.google.android.apps.authenticator.NetworkTimeProvider;
import com.google.android.apps.authenticator.PasscodeGenerator;
import com.google.android.apps.authenticator.TotpCounter;
import com.google.android.apps.authenticator.Base32String.DecodingException;
import com.google.android.apps.authenticator.PasscodeGenerator.Signer;

public class Authenticator {

	private static Signer getSigningOracle(String secret) {
	    try {
	    	byte[] keyBytes = decodeKey(secret);
	    	final Mac mac = Mac.getInstance("HMACSHA1");
	    	mac.init(new SecretKeySpec(keyBytes, ""));
	    	return new Signer() {
	    		public byte[] sign(byte[] data) {
	    			return mac.doFinal(data);
	    		}
	    	};
	    } catch (DecodingException error) {
	    	error.printStackTrace();
	    } catch (NoSuchAlgorithmException error) {
	    	error.printStackTrace();
	    } catch (InvalidKeyException error) {
	    	error.printStackTrace();
	    }
	    return null;
	  }
	
	private static byte[] decodeKey(String secret) throws DecodingException {
	    return Base32String.decode(secret);
	}
	 
	public static String computePin(String secret, long timeStep, byte[] challenge,boolean syncWithServer) throws Exception {
		long otp_state = 0;
		if (secret == null || secret.length() == 0) {
			throw new Exception("Null or empty secret");
		}
	    try {
	    	Signer signer = Authenticator.getSigningOracle(secret);
		    PasscodeGenerator pcg = new PasscodeGenerator(signer,(challenge == null) ? 6 : 9);
		    if(syncWithServer) {
		    	//use this constructor if you are under proxy
		    	//NetworkTimeProvider ntp = new NetworkTimeProvider("proxyaddress", port );
		    	//use this constructor if not under proxy
		    	NetworkTimeProvider ntp = new NetworkTimeProvider();
		    	long networkTime = ntp.getNetworkTime();
		    	otp_state = (new TotpCounter(timeStep)).getValueAtTime((networkTime + (60 * 1000))/1000);
		    } else { //get current system timestamp
		    	otp_state = (new TotpCounter(timeStep)).getValueAtTime((System.currentTimeMillis() + (60 * 1000))/1000);	
		    }
			
			System.out.println("otp_state " + otp_state);
		    return (challenge == null) ? pcg.generateResponseCode(otp_state) : pcg.generateResponseCode(otp_state, challenge);
		} catch (GeneralSecurityException e) {
			throw new Exception("Crypto failure", e);
		}
	}

}
