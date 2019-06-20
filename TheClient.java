package client;


import java.io.*;
import opencard.core.service.*;
import opencard.core.terminal.*;
import opencard.core.util.*;
import opencard.opt.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class TheClient {

	private PassThruCardService servClient = null;
	boolean DISPLAY = true;
	boolean loop = true;

	static final byte CLA					= (byte)0x00;
	static final byte P1					= (byte)0x00;
	static final byte P2					= (byte)0x00;
	static final byte UPDATECARDKEY				= (byte)0x14;
	static final byte UNCIPHERFILEBYCARD			= (byte)0x13;
	static final byte CIPHERFILEBYCARD			= (byte)0x12;
	static final byte CIPHERANDUNCIPHERNAMEBYCARD		= (byte)0x11;
	static final byte READFILEFROMCARD			= (byte)0x10;
	static final byte WRITEFILETOCARD			= (byte)0x09;
	static final byte UPDATEWRITEPIN			= (byte)0x08;
	static final byte UPDATEREADPIN				= (byte)0x07;
	static final byte DISPLAYPINSECURITY			= (byte)0x06;
	static final byte DESACTIVATEACTIVATEPINSECURITY	= (byte)0x05;
	static final byte ENTERREADPIN				= (byte)0x04;
	static final byte ENTERWRITEPIN				= (byte)0x03;
	static final byte READNAMEFROMCARD			= (byte)0x02;
	static final byte WRITENAMETOCARD			= (byte)0x01;
	static final byte DATAMAXSIZE				= (byte)0x7F;
	static final byte MAXBLOCKS				= (byte)0xFF;

	private final static byte CLA_TEST				= (byte)0x90;
	private final static byte INS_TESTDES_ECB_NOPAD_ENC       	= (byte)0x28;
	private final static byte INS_TESTDES_ECB_NOPAD_DEC       	= (byte)0x29;
	private final static byte INS_DES_ECB_NOPAD_ENC           	= (byte)0x20;
	private final static byte INS_DES_ECB_NOPAD_DEC           	= (byte)0x21;
	private final static byte P1_EMPTY = (byte)0x00;
	private final static byte P2_EMPTY = (byte)0x00;

	public TheClient() {
		try {
			SmartCard.start();
			System.out.print( "Smartcard inserted?... " );

			CardRequest cr = new CardRequest (CardRequest.ANYCARD,null,null);

			SmartCard sm = SmartCard.waitForCard (cr);

			if (sm != null) {
				System.out.println ("got a SmartCard object!\n");
			} else
				System.out.println( "did not get a SmartCard object!\n" );

			this.initNewCard( sm );

			SmartCard.shutdown();

		} catch( Exception e ) {
			System.out.println( "TheClient error: " + e.getMessage() );
		}
		java.lang.System.exit(0) ;
	}

	private ResponseAPDU sendAPDU(CommandAPDU cmd) {
		return sendAPDU(cmd, true);
	}

	private ResponseAPDU sendAPDU( CommandAPDU cmd, boolean display ) {
		ResponseAPDU result = null;
		try {
			result = this.servClient.sendCommandAPDU( cmd );
			if(display)
				displayAPDU(cmd, result);
		} catch( Exception e ) {
			System.out.println( "Exception caught in sendAPDU: " + e.getMessage() );
			java.lang.System.exit( -1 );
		}
		return result;
	}


	/************************************************
	 * *********** BEGINNING OF TOOLS ***************
	 * **********************************************/


	private String apdu2string( APDU apdu ) {
		return removeCR( HexString.hexify( apdu.getBytes() ) );
	}


	public void displayAPDU( APDU apdu ) {
		System.out.println( removeCR( HexString.hexify( apdu.getBytes() ) ) + "\n" );
	}


	public void displayAPDU( CommandAPDU termCmd, ResponseAPDU cardResp ) {
		System.out.println( "--> Term: " + removeCR( HexString.hexify( termCmd.getBytes() ) ) );
		System.out.println( "<-- Card: " + removeCR( HexString.hexify( cardResp.getBytes() ) ) );
	}


	private String removeCR( String string ) {
		return string.replace( '\n', ' ' );
	}


	/******************************************
	 * *********** END OF TOOLS ***************
	 * ****************************************/


	private boolean selectApplet() {
		boolean cardOk = false;
		try {
			CommandAPDU cmd = new CommandAPDU( new byte[] {
				(byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x00, (byte)0x0A,
				    (byte)0xA0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62,
				    (byte)0x03, (byte)0x01, (byte)0x0C, (byte)0x06, (byte)0x01
			} );
			ResponseAPDU resp = this.sendAPDU( cmd );
			if( this.apdu2string( resp ).equals( "90 00" ) )
				cardOk = true;
		} catch(Exception e) {
			System.out.println( "Exception caught in selectApplet: " + e.getMessage() );
			java.lang.System.exit( -1 );
		}
		return cardOk;
	}


	private void initNewCard( SmartCard card ) {
		if( card != null )
			System.out.println( "Smartcard inserted\n" );
		else {
			System.out.println( "Did not get a smartcard" );
			System.exit( -1 );
		}

		System.out.println( "ATR: " + HexString.hexify( card.getCardID().getATR() ) + "\n");


		try {
			this.servClient = (PassThruCardService)card.getCardService( PassThruCardService.class, true );
		} catch( Exception e ) {
			System.out.println( e.getMessage() );
		}

		System.out.println("Applet selecting...");
		if( !this.selectApplet() ) {
			System.out.println( "Wrong card, no applet to select!\n" );
			System.exit( 1 );
			return;
		} else
			System.out.println( "Applet selected" );

		mainLoop();
	}


	void updateCardKey() {
	}


	void uncipherFileByCard() {
	}


	void cipherFileByCard() {
	}


	void cipherAndUncipherNameByCard() {
	}


	void readFileFromCard() {

		String file_save_name = "";
		BufferedWriter bw = null;

		CommandAPDU cmd;
		ResponseAPDU resp = null;

		short parameter1 = 1;

		byte[] head = { CLA, READFILEFROMCARD, (byte) parameter1, P2, (byte)0 };

		cmd = new CommandAPDU( head );
		resp = this.sendAPDU( cmd, DISPLAY );

		byte[] rdata = resp.getBytes();


		for ( int i = 2; i < rdata.length-2; i++) {
			file_save_name += (char) rdata[i];
    }
		System.out.println(file_save_name);
		file_save_name += ".save";

    try {

			File file = new File(file_save_name);

			file.createNewFile();

			FileWriter file_save = new FileWriter(file);
			bw = new BufferedWriter(file_save);

      byte[] buff = new byte[DATAMAXSIZE-5];
      String file_block = "";

      while (rdata[0] != (byte) 3) { //Tant que c'est pas le dernier bloc

				parameter1 = 2;

				byte[] head2 = { CLA, READFILEFROMCARD, (byte) parameter1 , P2, (byte)0 };

	    	cmd = new CommandAPDU( head2 );
		  	resp = this.sendAPDU( cmd, DISPLAY );
				rdata = resp.getBytes();

		  	for ( int i = 2; i < rdata.length-2; i++) {
					file_block += (char) rdata[i];
      	}

				bw.write(file_block);
				file_block = "";

			}

      System.out.println("Recuperation done !");


       } catch (IOException e) {
       		e.printStackTrace();
       } finally {

         	try {
            	if (bw != null)
              bw.close();

	        } catch (IOException e) {
       		    e.printStackTrace();
			}
		}
	}

	void writeFileToCard() {

		String fileName = "test.txt";
		FileInputStream fis = null;

		CommandAPDU cmd;
		ResponseAPDU resp = null;

    try {

		  fis = new FileInputStream(new File(fileName));
      byte[] buff = new byte[DATAMAXSIZE-5];

			short parameter1 = 1;
			// Le paramètre P1 va spécifier pour la valeur :
			// 1 = nom fichier
			// 2 = bloc de taille DATAMAXSIZE-5
			// 3 = bloc de taille inférieure à DATAMAXSIZE-5

			byte[] head = { CLA, WRITEFILETOCARD, (byte) parameter1, P2, (byte) fileName.length() };
			byte[] cmd_1 = new byte[fileName.length()+5];
			byte[] fname = fileName.getBytes();

			System.arraycopy(head,(byte)0, cmd_1,(byte)0, 5);
			System.arraycopy(fname,(byte)0, cmd_1,(byte)5, (byte) (fileName.length()));

		  cmd = new CommandAPDU( cmd_1 );
		  resp = this.sendAPDU( cmd, DISPLAY );

			byte cpt_blocks = 0;
			short n = 0;
    	while ((n = (short) fis.read(buff)) != -1) {


			  if ( n == (DATAMAXSIZE-5)) {

					//si le buffer est plein, parameter1 = 2 pour indiquer que des blocs vont suivre
					parameter1 = (byte) 2;
					cpt_blocks++;

					if (cpt_blocks == MAXBLOCKS) {

						parameter1 = 1;
						n = 0;
						byte[] head2 = { CLA, WRITEFILETOCARD, (byte) parameter1 , P2, (byte) (n) };
	        	cmd = new CommandAPDU( head2 );
				    resp = this.sendAPDU( cmd, DISPLAY );
						break;
					}
				}

				// si le buffer n'est pas plein, parameter1 = 3 pour indiquer que c'est le dernier bloc
				if ( n < (DATAMAXSIZE-5) ) { parameter1 = (byte) 3;}

				byte[] head2 = { CLA, WRITEFILETOCARD, (byte) parameter1 , P2, (byte) (n) };
				byte[] cmd_2 = new byte[n+5];

				System.arraycopy(head2,(byte)0, cmd_2,(byte)0, 5);
				System.arraycopy(buff,(byte)0, cmd_2,(byte)5, (byte) (n));

	      cmd = new CommandAPDU( cmd_2 );

		    resp = this.sendAPDU( cmd, DISPLAY );

			}

  	System.out.println("Copy Success");

		} catch (FileNotFoundException e) {
         		e.printStackTrace();

      		} catch (IOException e) {
         		e.printStackTrace();

      		} finally {
         		try {
            			if (fis != null)
               				fis.close();

	         	} catch (IOException e) {
       		     		e.printStackTrace();
         		}
	   	}
	}


	void updateWritePIN() {
		String pin="";
	  byte[] bpin;
    CommandAPDU cmd;
		ResponseAPDU resp=null;
		pin=readKeyboard();
		bpin=pin.getBytes();

		byte[] header= { CLA,  UPDATEWRITEPIN, P1, P2, (byte) pin.length() };

		byte[] cmd_1= new byte[5 + pin.length()];

		System.arraycopy(header,(short)0,cmd_1,(short)0, (byte)5);
		System.arraycopy(bpin,(short)0,cmd_1,(byte)5, (byte) pin.length());

		cmd = new CommandAPDU( cmd_1 );

		resp = this.sendAPDU( cmd, DISPLAY );

	}


	void updateReadPIN() {

		String pin="";
	  byte[] bpin;
    CommandAPDU cmd;
		ResponseAPDU resp=null;

		pin=readKeyboard();
		bpin=pin.getBytes();

		byte[] header= { CLA, UPDATEREADPIN, P1, P2, (byte) pin.length() };

		byte[] cmd_1= new byte[5 + pin.length()];

		System.arraycopy(header,(short)0,cmd_1,(short)0, (byte)5);
		System.arraycopy(bpin,(short)0,cmd_1,(byte)5, (byte) pin.length());

		cmd = new CommandAPDU( cmd_1 );

		resp = this.sendAPDU( cmd, DISPLAY );

	}


	void displayPINSecurity() {
		CommandAPDU cmd;
		ResponseAPDU resp=null;

		byte[] header= { CLA, DISPLAYPINSECURITY, P1, P2, (byte) 0 };
		cmd = new CommandAPDU( header );
		resp = this.sendAPDU( cmd, DISPLAY );
	}


	void desactivateActivatePINSecurity() {
		CommandAPDU cmd;
		ResponseAPDU resp=null;

		byte[] header= { CLA, DESACTIVATEACTIVATEPINSECURITY, P1, P2};
		cmd = new CommandAPDU( header );
		resp = this.sendAPDU( cmd, DISPLAY);
	}


	void enterReadPIN() {

		String pin="";
	  byte[] bpin;
    CommandAPDU cmd;
		ResponseAPDU resp=null;

		pin=readKeyboard();
		bpin=pin.getBytes();

		byte[] header= { CLA,  ENTERREADPIN, P1, P2, (byte) pin.length() };

		byte[] cmd_1= new byte[5 + pin.length()];

		System.arraycopy(header,(short)0,cmd_1,(short)0, (byte)5);
		System.arraycopy(bpin,(short)0,cmd_1,(byte)5, (byte) pin.length());

		cmd = new CommandAPDU( cmd_1 );

		resp = this.sendAPDU( cmd, DISPLAY );

		displayAPDU(cmd, resp);

	}


	void enterWritePIN() {

		String pin="";
	  byte[] bpin;
    CommandAPDU cmd;
		ResponseAPDU resp=null;

		pin=readKeyboard();
		bpin=pin.getBytes();

		byte[] header= { CLA,  ENTERWRITEPIN, P1, P2, (byte) pin.length() };

		byte[] cmd_1= new byte[5 + pin.length()];

		System.arraycopy(header,(short)0,cmd_1,(short)0, (byte)5);
		System.arraycopy(bpin,(short)0,cmd_1,(byte)5, (byte) pin.length());

		cmd = new CommandAPDU( cmd_1 );

		resp = this.sendAPDU( cmd, DISPLAY );

		displayAPDU(cmd, resp);

	}


	void readNameFromCard() {
		CommandAPDU cmd;
		ResponseAPDU resp=null;

		byte[] header= { CLA, READNAMEFROMCARD, P1, P2, (byte)0 };
		cmd = new CommandAPDU( header );
		resp = this.sendAPDU( cmd, DISPLAY );

	}


	void writeNameToCard() {
		String name="";
	  byte[] bname;
    CommandAPDU cmd;
		ResponseAPDU resp=null;

		name=readKeyboard();
		bname=name.getBytes();


		byte[] header= { CLA, WRITENAMETOCARD, P1, P2, (byte) name.length() };

		byte[] cmd_1= new byte[5 + name.length()];

		System.arraycopy(header,(short)0,cmd_1,(short)0, (byte)5);
		System.arraycopy(bname,(short)0,cmd_1,(byte)5, (byte) name.length());


		cmd = new CommandAPDU( cmd_1 );
		resp = this.sendAPDU( cmd, DISPLAY );

		displayAPDU(cmd, resp);

	}


	void exit() {
		loop = false;
	}


	void runAction( int choice ) {
		switch( choice ) {
			case 14: updateCardKey(); break;
			case 13: uncipherFileByCard(); break;
			case 12: cipherFileByCard(); break;
			case 11: cipherAndUncipherNameByCard(); break;
			case 10: readFileFromCard(); break;
			case 9: writeFileToCard(); break;
			case 8: updateWritePIN(); break;
			case 7: updateReadPIN(); break;
			case 6: displayPINSecurity(); break;
			case 5: desactivateActivatePINSecurity(); break;
			case 4: enterReadPIN(); break;
			case 3: enterWritePIN(); break;
			case 2: readNameFromCard(); break;
			case 1: writeNameToCard(); break;
			case 0: exit(); break;
			default: System.out.println( "unknown choice!" );
		}
	}


	String readKeyboard() {
		String result = null;

		try {
			BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );
			result = input.readLine();
		} catch( Exception e ) {}

		return result;
	}


	int readMenuChoice() {
		int result = 0;

		try {
			String choice = readKeyboard();
			result = Integer.parseInt( choice );
		} catch( Exception e ) {}

		System.out.println( "" );

		return result;
	}


	void printMenu() {
		System.out.println( "" );
		System.out.println( "14: update the DES key within the card" );
		System.out.println( "13: uncipher a file by the card" );
		System.out.println( "12: cipher a file by the card" );
		System.out.println( "11: cipher and uncipher a name by the card" );
		System.out.println( "10: read a file from the card" );
		System.out.println( "9: write a file to the card" );
		System.out.println( "8: update WRITE_PIN" );
		System.out.println( "7: update READ_PIN" );
		System.out.println( "6: display PIN security status" );
		System.out.println( "5: desactivate/activate PIN security" );
		System.out.println( "4: enter READ_PIN" );
		System.out.println( "3: enter WRITE_PIN" );
		System.out.println( "2: read a name from the card" );
		System.out.println( "1: write a name to the card" );
		System.out.println( "0: exit" );
		System.out.print( "--> " );
	}


	void mainLoop() {
		while( loop ) {
			printMenu();
			int choice = readMenuChoice();
			runAction( choice );
		}
	}


	public static void main( String[] args ) throws InterruptedException {
		new TheClient();
	}



}
