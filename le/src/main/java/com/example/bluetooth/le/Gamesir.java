package com.example.bluetooth.le;


public class Gamesir {

	static {
		System.loadLibrary("Gamesir");
	}

	public native static int[] decryJoyData(int[] data);

	public native static int[] decryJoyDataYuneec(int[] data);

	public native static void setBTMac(byte[] macAddr);

	public native static int[] decryJoytouchData(int[] data);

}
