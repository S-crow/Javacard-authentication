package applet;


import javacard.framework.*;

import javacard.security.*;
import javacardx.crypto.*;

public class TheApplet extends Applet {


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
	static final byte MAXDATASIZE				= (byte)0x7F;

	byte[] bname=new byte[50];
	byte[] bpin=new byte[50];
	byte[] bfilename=new byte[50];
	byte[] contenu= new byte[100];

	final static short SW_VERIFICATION_FAILED = (short) 0x6300;
	final static short SW_PIN_VERIFICATION_REQUIRED = (short) 0x6301;

	byte[] file_mem = new byte[0xFF*(MAXDATASIZE-5)];
	short nb_block = 0;
	short total_block = 0;
	short last_block_size = 0;


	OwnerPIN READpin, WRITEpin, pin;

	boolean pinSec=true;  //Securite activee par defaut

	protected TheApplet() {
		byte[] pincode = {(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30}; // PIN code "0000" READ
		READpin = new OwnerPIN((byte)3,(byte)8);
		READpin.update(pincode,(short)0,(byte)4);

		byte[] pincode2 = {(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31}; // PIN code "1111" WRITE
		WRITEpin = new OwnerPIN((byte)3,(byte)8);
		WRITEpin.update(pincode2,(short)0,(byte)4);

		this.register();
	}


	public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException {
		new TheApplet();
	}


	public boolean select() {

	if ( (READpin.getTriesRemaining() == 0) || (WRITEpin.getTriesRemaining() == 0) )
			return false;

		return true;
	}


	public void deselect() {
		READpin.reset();
		WRITEpin.reset();
	}


	public void process(APDU apdu) throws ISOException {
		if( selectingApplet() == true )
			return;

		byte[] buffer = apdu.getBuffer();

		switch( buffer[1] ) 	{
			case UPDATECARDKEY: updateCardKey( apdu ); break;
			case UNCIPHERFILEBYCARD: uncipherFileByCard( apdu ); break;
			case CIPHERFILEBYCARD: cipherFileByCard( apdu ); break;
			case CIPHERANDUNCIPHERNAMEBYCARD: cipherAndUncipherNameByCard( apdu ); break;
			case READFILEFROMCARD: readFileFromCard( apdu ); break;
			case WRITEFILETOCARD: writeFileToCard( apdu ); break;
			case UPDATEWRITEPIN: updateWritePIN( apdu ); break;
			case UPDATEREADPIN: updateReadPIN( apdu ); break;
			case DISPLAYPINSECURITY: displayPINSecurity( apdu ); break;
			case DESACTIVATEACTIVATEPINSECURITY: desactivateActivatePINSecurity( apdu ); break;
			case ENTERREADPIN: enterReadPIN( apdu ); break;
			case ENTERWRITEPIN: enterWritePIN( apdu ); break;
			case READNAMEFROMCARD: readNameFromCard( apdu ); break;
			case WRITENAMETOCARD: writeNameToCard( apdu ); break;
			default: ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}


	void updateCardKey( APDU apdu ) {
	}


	void uncipherFileByCard( APDU apdu ) {
	}


	void cipherFileByCard( APDU apdu ) {
	}


	void cipherAndUncipherNameByCard( APDU apdu ) {
	}


	void readFileFromCard( APDU apdu ) {

		byte[] buffer = apdu.getBuffer();
		short offset = 0;

		switch (buffer[2]) {

			case (short) 1:	//On recupere le nom de fichier

						 nb_block = (short) 0;
		 				 buffer[0] = (byte) 1;
		 				 buffer[1] = (byte) (file_mem[0]); // taille du nom de fichier
		 				 Util.arrayCopy(file_mem, (byte) 1, buffer, (byte)2, (byte) (file_mem[0])); // Recup Nom Fichier
		 				 total_block = (short) (file_mem[file_mem[0]+1]);

		 				 apdu.setOutgoingAndSend((short)0, (short) (file_mem[0] + 2));
				     break;

			case (short) 2: //On recupere les blocs data
							// S'il reste au moins un bloc à envoyer
							if (total_block > 0) {
										buffer[0] = (byte) 2;
										buffer[1] = (byte) (MAXDATASIZE-5);

										offset = (short) ((file_mem[0] + 3) + (short) (((short) nb_block)*((short) ((MAXDATASIZE-5)))));

										Util.arrayCopy(file_mem, (short) (offset), buffer, (byte)2, (byte) (MAXDATASIZE-5)); // Recup block

										nb_block++;
										total_block--;

										// envoi de MAXDATASIZE-3 car 2 octets d'offset
										apdu.setOutgoingAndSend((short)0, (short) (MAXDATASIZE-3));

				       break;

		}

		 		buffer[0] = (byte) 3;

				byte size_last = file_mem[ (byte) ((byte) file_mem[0] + (byte) 2) ];
				buffer[1] = (byte) size_last;
				// indice du dernier bloc : taille nom fichier + nom fichier + 2 octets (nb de blocs et taille du dernier bloc) + taille occupee par blocs de MAXDATASIZE-5
				offset = (short) ((file_mem[0] + 3) + (nb_block*(MAXDATASIZE-5)));
				Util.arrayCopy(file_mem, (short) (offset), buffer, (byte)2, (byte) (file_mem[file_mem[0]+ 3]));

				apdu.setOutgoingAndSend((short)0, (short) ((file_mem[file_mem[0]+ 2]) + (short) 2));

			}

		}


	void writeFileToCard( APDU apdu ) {

		byte[] buffer = apdu.getBuffer();
		apdu.setIncomingAndReceive();

		// offset = 1er octet (taille nom fichier) + nom fichier + nb de blocs + taille du dernier bloc + nb_blocs*taille_bloc
		short ind = (short) ((((short) file_mem[0]) + (short) 3) + ((short) (((short) nb_block) * (byte) (MAXDATASIZE-5))));

		switch (buffer[2]) {

			case (short) 1:	//On recupere le nom de fichier
						 file_mem[0] = buffer[4];
						 Util.arrayCopy(buffer, (byte)5, file_mem, (byte)1, (byte) buffer[4]); // Recup Nom Fichier
						 nb_block=0;
				     break;

			case (short) 2: //On recupere les blocs data

						 Util.arrayCopy(buffer, (byte)5, file_mem, (short) ind, (byte) buffer[4]);
						 nb_block++;
				     break;

			case (short) 3: //On recupere le dernier bloc

				Util.arrayCopy(buffer, (byte)5, file_mem, (short) ind, (byte) buffer[4]);

						// on ecrit le nb de blocs total apres le nom de fichier
						file_mem[file_mem[0]+1] = (byte) (nb_block & 0xFF);

						//et juste à la suite on écrit la taille du dernier bloc
						file_mem[file_mem[0]+2] = (byte) (buffer[4] & 0xFF);
				     break;
			default:
				     break;
		}
	}


	void updateWritePIN( APDU apdu ) {
		if(pinSec){
			if(!WRITEpin.isValidated()){
				ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
			}
		}

		byte[] buffer = apdu.getBuffer();
		apdu.setIncomingAndReceive();

		WRITEpin.update(buffer, (short) 5, (byte) 4);
	}


	void updateReadPIN( APDU apdu ) {
		if(pinSec){
			if(!READpin.isValidated()){
				ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
			}
		}

		byte[] buffer = apdu.getBuffer();
		apdu.setIncomingAndReceive();

		READpin.update(buffer, (short) 5, (byte) 4);

	}


	void displayPINSecurity( APDU apdu ) {
		byte[] buffer = apdu.getBuffer();

		buffer[0]=(pinSec?(byte)1:(byte)0);

		buffer[1]=(byte) 0;
		apdu.setOutgoingAndSend((short) 0, (byte) 2 );

	}


	void desactivateActivatePINSecurity( APDU apdu ) {
		pinSec= !pinSec;
	}



	void verify( APDU apdu, OwnerPIN pin ) {
		apdu.setIncomingAndReceive();
		byte[] buffer = apdu.getBuffer();
		if( !pin.check( buffer, (byte)5, buffer[4] ) ) {
			ISOException.throwIt( SW_VERIFICATION_FAILED );
		}
	}


	void enterReadPIN( APDU apdu ) {
		verify(apdu, READpin);
	}


	void enterWritePIN( APDU apdu ) {
		verify(apdu, WRITEpin);

	}


	void readNameFromCard( APDU apdu ) {

		if (pinSec== true){

			if( !READpin.isValidated()){
				ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);

			}
		}

		byte[] buffer = apdu.getBuffer();
		apdu.setIncomingAndReceive();

		Util.arrayCopy(bname, (byte)1, buffer, (short)0, bname[0]);

		apdu.setOutgoingAndSend( (short)0, bname[0] );

	}


	void writeNameToCard( APDU apdu ) {
		if (pinSec== true){

			if( !WRITEpin.isValidated()){
				ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);

			}
		}

		byte[] buffer = apdu.getBuffer();

		apdu.setIncomingAndReceive();
		Util.arrayCopy(buffer, (byte)4, bname, (byte)0, (byte)1);
		Util.arrayCopy(buffer, (byte)5, bname, (byte)1, (byte) buffer[4]);

	}


}
