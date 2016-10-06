package com.viswa.authenticator;

public class TestAuthenticator {

	public static void main(String[] args) {
		try {
			//secret code specific to user
			String secretCode = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";
			//denotes 30 milliseconds
			long timeStep = 30L;
			//to sync up with google time pass last parameter as true
			System.out.println("output now --> " + Authenticator.computePin(secretCode,timeStep,null,true));
			//to get the pin with current system time pass last parameter as false
			System.out.println("output now --> " + Authenticator.computePin(secretCode,timeStep,null,false));
		} catch(Exception e) { 
			e.printStackTrace();
		}
	}
}
