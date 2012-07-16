package moco;
/*
 * Moco: A Midi-OSC OSC-Midi converter
 * By Rob King 2010
 */
import javax.sound.midi.MidiMessage;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import themidibus.MidiBus;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.RadioButton;
import controlP5.Textfield;
import controlP5.Toggle;


public class Moco extends PApplet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1816549495367020936L;
	MidiBus midi = null;
	OscP5 osc = null;
	int oscRecievePort = 5005;
	
	NetAddress oscOuptutAddress;
	NetAddress oscLoopbackAddress;
	
	String[] midiInputs;
	String[] midiOutputs;
	ControlP5 controlP5;
	
	Textfield cOSCRecievePort;
	Textfield cOSCSendPort;
	Textfield cOSCIP;
	RadioButton cMIDIIn;
	RadioButton cMIDIOut;
	Toggle cSendRaw;
	
	boolean midiRecieved = false;
	boolean oscRecieved  = false;
	
	boolean sendRawMidi = false;
	
	public void setup() {
		size(560,300);
		midi = new MidiBus(this);
		midiInputs = MidiBus.availableInputs();
		midiOutputs = MidiBus.availableOutputs();
		osc = new OscP5(this, oscRecievePort);
		oscOuptutAddress = new NetAddress("127.0.0.1", 5000);
		oscLoopbackAddress = new NetAddress("127.0.0.1", oscRecievePort);

		
		frame.setTitle("Moco");
		
		
		controlP5 = new ControlP5(this);

		
		controlP5.addTextlabel("MidiIns", "Midi Inputs", 10, 20);
		cMIDIIn = controlP5.addRadioButton("midiInput", 10, 35);
		for(int i = 0; i<midiInputs.length; i++)
			cMIDIIn.addItem(midiInputs[i], i);
		
		controlP5.addTextlabel("Title", "Moco : MIDI - OSC Converter : addi.tv ", 2, 2);
		
		controlP5.addTextlabel("OSC Output", "OSC Output", 150, 20);
		cOSCSendPort = controlP5.addTextfield("OSC Sending Port", 150, 65, 100, 15);
		cOSCSendPort.setText(Integer.toString(oscOuptutAddress.port()));
		cOSCSendPort.setAutoClear(false);
		cOSCIP = controlP5.addTextfield("OSC Sending Address", 150, 35, 100, 15);
		cOSCIP.setText(oscOuptutAddress.address());
		cOSCIP.setAutoClear(false);
		cSendRaw = controlP5.addToggle("Send Raw Midi", sendRawMidi, 150, 95, 100, 15);
		
		controlP5.addTextlabel("OSC Input", "OSC Input", 300, 20);
		cOSCRecievePort = controlP5.addTextfield("OSC Listening Port", 300, 35, 100, 15);
		cOSCRecievePort.setText(Integer.toString(oscRecievePort));
		cOSCRecievePort.setAutoClear(false);
		
		
		controlP5.addTextlabel("MidiOuts", "Midi Outputs", 420, 20);
		cMIDIOut = controlP5.addRadioButton("midiOutput", 420, 35);
		for(int i = 0; i<midiOutputs.length; i++)
			cMIDIOut.addItem(midiOutputs[i], i);
		
		
		controlP5.addTextarea("description", "OSC Namespace:\n\n" +
				"/midi/noteon \n" +
				"   int:channel\n" +
				"   int:pitch\n" +
				"   int:velocity\n\n" +
				"/midi/noteoff \n" +
				"   int:channel\n" +
				"   int:pitch\n" +
				"   int:velocity\n\n" +
				"/midi/cc \n" +
				"   int:channel\n" +
				"   int:cc#\n" +
				"   int:value\n\n" +
				"/midi/raw \n" +
				"   ints:data\n" +
				"",  300, 90, 100, 200);
		
		
	}
	
	public void changeMIDIInput(String deviceName){
		midi.clearInputs();
		if(deviceName!=null){
			midi.addInput(deviceName);
			println("MidiDeviceChanged to "+deviceName);
		}
	}
	public void changeMIDIOutput(String deviceName){
		midi.clearOutputs();
		if(deviceName!=null){
			midi.addOutput(deviceName);
			println("MidiDeviceChanged to "+deviceName);
		}
	}
	
	public void changeOSCOutputAddress(String ip, int port){
		oscOuptutAddress = new NetAddress(ip, port);

	}
	public void changeOSCOutputAddress(String ip){
		oscOuptutAddress = new NetAddress(ip, oscOuptutAddress.port());

		println("Changing destination ip to " + ip);
	}
	public void changeOSCOutputPort(int port){
		oscOuptutAddress = new NetAddress(oscOuptutAddress.address(), port);
	}
	public void changeOSCInputPort(int port){
		osc.stop();
		osc = new OscP5(this, port);
	}
	
	public void draw() {
		background(150);
		fill(100);
		stroke(255);

		
		if(midiRecieved){
			stroke(0, 255,0);
			midiRecieved = false;
		}else{
			stroke(255);
		}
		rect(5,15,250,height-20);
		line(70,22,140,22);
		line(140,22, 137,25);
		line(140,22, 137,19);
		
		if(oscRecieved){
			stroke(0, 255,0);
			oscRecieved = false;
		}else{
			stroke(255);
		}
		rect(295,15,255,height-20);
		line(350,22,410,22);
		line(410,22, 407,25);
		line(410,22, 407,19);
		
			
	}
	
	public void controlEvent(ControlEvent theEvent) {
		if(theEvent.isController()){
			if(theEvent.controller() == cOSCRecievePort){
				boolean canParsePort = false;
				try{
					Integer.valueOf(cOSCRecievePort.getText());
					canParsePort = true;
				}catch(NumberFormatException exc){
					System.err.println("Couldn't parse recieve port");
					cOSCRecievePort.setText(Integer.toString(oscRecievePort));
				}
				if(canParsePort)
					changeOSCInputPort(Integer.valueOf(cOSCRecievePort.getText()));
			}
			if(theEvent.controller() == cOSCSendPort){
				boolean canParsePort = false;
				try{
					Integer.valueOf(cOSCSendPort.getText());
					canParsePort = true;
				}catch(NumberFormatException exc){
					System.err.println("Couldn't parse recieve port");
					cOSCSendPort.setText(Integer.toString(oscOuptutAddress.port()));
				}
				if(canParsePort)
					changeOSCOutputPort(Integer.valueOf(cOSCSendPort.getText()));
			}
			if(theEvent.controller() == cOSCIP)
				changeOSCOutputAddress(cOSCIP.getText());
			if(theEvent.controller() == cSendRaw)
				sendRawMidi = cSendRaw.getState();
		}
		if(theEvent.isGroup()){
			if(theEvent.group() == cMIDIIn){
				if(theEvent.group().value() == -1)
					changeMIDIInput(null);
				else
					changeMIDIInput(midiInputs[(int)theEvent.group().value()]);
			}
			if(theEvent.group() == cMIDIOut){
				if(theEvent.group().value() == -1)
					changeMIDIOutput(null);
				else
					changeMIDIOutput(midiOutputs[(int)theEvent.group().value()]);
			}
		}
		  
	}
	

	public void noteOn(int channel, int pitch, int velocity) {
		
		OscMessage msg = new OscMessage("/midi/noteon");
		msg.add(channel);
		msg.add(pitch);
		msg.add(velocity);
		try{
			osc.send(msg, oscOuptutAddress);
			osc.send(msg, oscLoopbackAddress);

			println("Sent " + msg + " to " + oscOuptutAddress);
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		OscMessage msg2 = new OscMessage("/midi/noteon/"+channel+"/"+pitch);

		msg2.add(velocity);
		try{
			osc.send(msg2, oscOuptutAddress);
			osc.send(msg2, oscLoopbackAddress);

		}catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void noteOff(int channel, int pitch, int velocity) {
		OscMessage msg = new OscMessage("/midi/noteoff");
		msg.add(channel);
		msg.add(pitch);
		msg.add(velocity);
		try{
			osc.send(msg, oscOuptutAddress);
			osc.send(msg, oscLoopbackAddress);

		}catch (Exception e) {
			// TODO: handle exception
		}
		
		OscMessage msg2 = new OscMessage("/midi/noteoff/"+channel+"/"+pitch);

		msg2.add(velocity);
		try{
			osc.send(msg2, oscOuptutAddress);
			osc.send(msg2, oscLoopbackAddress);

		}catch (Exception e) {
			// TODO: handle exception
		}
		
		
	}

	public void controllerChange(int channel, int number, int value) {
		OscMessage msg = new OscMessage("/midi/cc");
		msg.add(channel);
		msg.add(number);
		msg.add(value);
		try{
			osc.send(msg, oscOuptutAddress);
			osc.send(msg, oscLoopbackAddress);

		}catch (Exception e) {
			// TODO: handle exception
		}
		
		OscMessage msg2 = new OscMessage("/midi/cc/"+channel+"/"+number);
		//msg2.add(channel);
		//msg2.add(number);
		msg2.add(value);
		try{
			osc.send(msg2, oscOuptutAddress);
			osc.send(msg2, oscLoopbackAddress);

		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public void midiMessage(MidiMessage message) { 
		midiRecieved = true;
		OscMessage oscMessage = new OscMessage("/midi/raw");
		//print ("Sent: " );
		for(int i = 0; i<message.getMessage().length; i++){
			oscMessage.add(message.getMessage()[i]);
			//print(message.getMessage()[i] + " ");
		}
		//println("");
		if(sendRawMidi){
			try{
				osc.send(oscMessage, oscOuptutAddress);
				osc.send(oscMessage, oscLoopbackAddress);

			}catch (Exception e) {
				// TODO: handle exception
			}
		}
		
	}
	
	public void oscEvent(OscMessage theOscMessage) {
		  /* print the address pattern and the typetag of the received OscMessage */
		//println(theOscMessage);
		  oscRecieved = true;
			  if(theOscMessage.checkAddrPattern("/midi/raw")){
				 
				 int rawLength = theOscMessage.typetag().length();
				 byte[] midiData = new byte[rawLength];
				 //print("Got:  ");
				 for(int i = 0; i<rawLength; i++){
					 midiData[i] = (byte) theOscMessage.get(i).intValue();
					//print((byte) theOscMessage.get(i).intValue() + " "); 
				 }
				 println(" : RAW");
				 	
				  
				 midi.sendMessage(midiData);
				 
		  	}

			  if(theOscMessage.checkAddrPattern("/midi/noteon")){
				  println("Sending noteon to "+midi);
				  println(theOscMessage.get(0).intValue() + " "+  theOscMessage.get(1).intValue()+ " " + theOscMessage.get(2).intValue());
				  midi.sendNoteOn(theOscMessage.get(0).intValue(), theOscMessage.get(1).intValue(), theOscMessage.get(2).intValue()) ;
			  }
			  if(theOscMessage.checkAddrPattern("/midi/noteoff")){
				  midi.sendNoteOff(theOscMessage.get(0).intValue(), theOscMessage.get(1).intValue(), theOscMessage.get(2).intValue()) ;
			  }
			  if(theOscMessage.checkAddrPattern("/midi/cc")){
				  midi.sendControllerChange(theOscMessage.get(0).intValue(), theOscMessage.get(1).intValue(), theOscMessage.get(2).intValue()) ;
			  }
	}
	
	
	
	public static void main(String _args[]) {
		PApplet.main(new String[] { moco.Moco.class.getName() });
	}
}
