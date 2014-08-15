package processing.test.meu_text;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ketai.ui.*; 
import android.content.Intent; 
import android.os.Bundle; 
import apwidgets.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class MeU_Text extends PApplet {

//////////////////////////////////////////////////////////////////////////
//Filenames: MeU.pde
//Authors: Robert Tu
//Date Created: January 23, 2014
//Notes:
/*
  
  This is the Processing Android program that controls the MeU panel. It
  utilizes the android processing library, ketai library for the selection, 
  list, apwidgets for UI devices such as buttons and input boxes and Btserial
  to communicate with the arduino bluetooth. 
  
  In the main loop the program waits for a serial message sent from a mobile
  device via bluetooth. The message is then parsed for specific commands. 
  
  The message protocol is as follows:
  
  "bFFFFFFmessage\n\r"
  
  The first character is the mode command and will determine what kind of
  information will be displayed on the LED panel. Due to memory limitations
  of the Arduino Mini Pro, the only mode available is text display which is
  initiated by prefixing each command with "b". If you are sending a command 
  to the Arduino always prefix the message with "b".
  
  The next six characters are colour values in hex (RGB notation). The colour 
  of the text is determined by this value.
  
  The rest of the following characters is the actual text to be displayed on 
  the LED panel. 
  
  The \n\r characters are for the serial read andparsing function to determine 
  the end of the message.
  
  For example if the Arduino receives the following command:
  "bFF00FFHello there how are you?\n\r"
  
  MeU will display "Hello there how are you?" in magenta colour.
  
  PLEASE NOTE: The Arduino Mini only has 2K of SRAM and since the Adafruit library
  uses much of that, any remaining dynamic variables must be used
  very wisely.


*/

//////////////////////////////////////////////////////////////////////////


//import libraries


//Ketai Sensor Library for Android: http://KetaiProject.org by Daniel Sauter





//APwidgets library https://code.google.com/p/apwidgets/ by Rikard Lundstedt

//BtSerial Copyright 2011, 2012 Andreas Goransson & David Cuartielles & Tom Igoe & Joshua Albers
//Version 0.2.0
//August 2012
//https://github.com/arduino/BtSerial

//selected bluetooth mac address to communicate with
String remoteAddress = "";

BtSerial bt;

//variable to assemble message to send to MeU panel
String MessageToSend = " ";

//flag to determine 
boolean SendFlag;

APWidgetContainer widgetContainer; 

APButton SendBtn;
APButton DeviceBtn;
APButton ClearBtn;
APButton SparkleBtn;
APButton MessageBtn1;
APButton MessageBtn2;
APButton MessageBtn3;
APButton MessageBtn4;
APButton SaveBtn1;
APButton SaveBtn2;
APButton SaveBtn3;
APButton SaveBtn4;
APEditText InputField;

KetaiList selectionList;

String [] deviceList;
StringDict colourList;
String selectedColour = "FFFFFF";

// timer variables
long savedTime;
int delayValue = 500;

//constants for button width and sizes
//these sizes should fit within Galaxy Note 3 resolution size

final int INPUT_W = 910;
final int INPUT_H = 135;
final int CTRL_BTN_W = 400;
final int BTN_H = 175;
final int MSG_BTN_W = 590;
final int SAVE_BTN_W = 260;

//x y positions for buttons
final int Y_INPUT = 170;

final int X_LEFT = 85;
final int X_CMD = 590;
final int X_SAVE = 725;

final int Y_CMD_1 = 355;
final int Y_CMD_2 = 575;
final int Y_MSG_1 = 900;
final int Y_MSG_2 = 1125;
final int Y_MSG_3 = 1335;
final int Y_MSG_4 = 1560;

final int Y_STATUS = 80;
final int Y_LABEL = 810;


public void setup()
{   
  orientation(PORTRAIT);
  
  stroke(255);
  textSize(40);
  
  bt = new BtSerial(this);
  
  colourList = new StringDict();
  colourList.set("Red", "FF0000");
  colourList.set("Green", "00FF00");
  colourList.set("Blue", "0000FF");
  colourList.set("Yellow", "FFFF00");
  colourList.set("Cyan", "00FFFF");
  colourList.set("Magenta", "FF00FF");
  colourList.set("White", "FFFFFF");
  
  
  widgetContainer = new APWidgetContainer(this); //create new container for widgets
  
  InputField = new APEditText(X_LEFT, Y_INPUT, INPUT_W, INPUT_H);
  
  SendBtn = new APButton(X_LEFT, Y_CMD_1, CTRL_BTN_W, BTN_H, "Send");
  DeviceBtn = new APButton(X_CMD, Y_CMD_1, CTRL_BTN_W, BTN_H, "Device");
  ClearBtn = new APButton(X_LEFT, Y_CMD_2, CTRL_BTN_W, BTN_H, "Clear Disp");
  SparkleBtn = new APButton(X_CMD, Y_CMD_2, CTRL_BTN_W, BTN_H, "Colour");
  
  MessageBtn1 = new APButton (X_LEFT, Y_MSG_1, MSG_BTN_W, BTN_H, loadText(1));
  MessageBtn2 = new APButton (X_LEFT, Y_MSG_2, MSG_BTN_W, BTN_H, loadText(2));
  MessageBtn3 = new APButton (X_LEFT, Y_MSG_3, MSG_BTN_W, BTN_H, loadText(3));
  MessageBtn4 = new APButton (X_LEFT, Y_MSG_4, MSG_BTN_W, BTN_H, loadText(4));
  SaveBtn1 = new APButton(X_SAVE, Y_MSG_1, SAVE_BTN_W, BTN_H, "Save 1");
  SaveBtn2 = new APButton(X_SAVE, Y_MSG_2, SAVE_BTN_W, BTN_H, "Save 2");
  SaveBtn3 = new APButton(X_SAVE, Y_MSG_3, SAVE_BTN_W, BTN_H, "Save 3");
  SaveBtn4 = new APButton(X_SAVE, Y_MSG_4, SAVE_BTN_W, BTN_H, "Save 4");

  widgetContainer.addWidget(InputField);
  widgetContainer.addWidget(SendBtn);
  widgetContainer.addWidget(DeviceBtn);
  widgetContainer.addWidget(ClearBtn);
  widgetContainer.addWidget(SparkleBtn);
  
  widgetContainer.addWidget(MessageBtn1);
  widgetContainer.addWidget(MessageBtn2);
  widgetContainer.addWidget(MessageBtn3);
  widgetContainer.addWidget(MessageBtn4);
  widgetContainer.addWidget(SaveBtn1);
  widgetContainer.addWidget(SaveBtn2);
  widgetContainer.addWidget(SaveBtn3);
  widgetContainer.addWidget(SaveBtn4);
  
  //Load drop down list
  deviceList = bt.list(true); 
  println(deviceList);

}

public void draw() {
  background(192, 241, 252);
  fill(0);
  text("Select a Message: ", X_LEFT, Y_LABEL);
  if (remoteAddress.equals("")){
    fill(255, 0, 0);
    text("Choose a MeU address.", X_LEFT, Y_STATUS);
  } else {
    if (SendFlag == true) {
      
      if(bt.isConnected() == true) {
        bt.write(MessageToSend);
        fill(0);
        text("Message sent!", X_LEFT, Y_STATUS);
        if ((millis() - savedTime) > delayValue) {
          bt.disconnect();
          SendFlag = false;
        }
        
        
      } else {
        if ((millis() - savedTime) > delayValue) {
          fill(255,0,0);
          text ("Cannot connect! Check if MeU is on.", X_LEFT, Y_STATUS);
          
        } else {
          fill(0);
          text ("Sending...", X_LEFT, Y_STATUS);
          
        }
      }
    } else {
      fill(0);
      text("Type your message: ", X_LEFT, Y_STATUS);
    }
  }
  
}
  
public void onClickWidget(APWidget widget){
  
  if (widget == SendBtn) {
   
    bt.connect(remoteAddress);
    MessageToSend = "b" + selectedColour + InputField.getText() + '\r' + '\n';
    SendFlag = true;
    StartTimer();
    
  } else if (widget == DeviceBtn) {
    deviceList = bt.list(true);
    selectionList = new KetaiList(this, deviceList); 
  } else if (widget == ClearBtn) {
    bt.connect(remoteAddress);
    MessageToSend = "b" + selectedColour + " " + '\r' + '\n';
    SendFlag = true;
    StartTimer();
  
  } else if (widget == SparkleBtn) {
    selectionList = new KetaiList(this, colourList.keyArray());
    
  } else if (widget == MessageBtn1) {
    InputField.setText(loadText(1));
  } else if (widget == MessageBtn2) {
    InputField.setText(loadText(2));
  } else if (widget == MessageBtn3) {
    InputField.setText(loadText(3));
  } else if (widget == MessageBtn4) {
    InputField.setText(loadText(4));
  } else if (widget == SaveBtn1) {
    MessageBtn1.setText(InputField.getText());
    saveText(1);
  } else if (widget == SaveBtn2) {
    MessageBtn2.setText(InputField.getText());
    saveText(2);
  } else if (widget == SaveBtn3) {
    MessageBtn3.setText(InputField.getText());
    saveText(3);  
  } else if (widget == SaveBtn4) {
    MessageBtn4.setText(InputField.getText());
    saveText(4);
  }
  
}

public void onKetaiListSelection(KetaiList kList) {
  //println(kList.getSelection());
  if (kList.getSelection().indexOf(":") != -1) {
    println(kList.getSelection());
    String[] DeviceInfo = split(kList.getSelection(), ',');
    remoteAddress = DeviceInfo[0];
    println(remoteAddress);
    String DeviceName = DeviceInfo[1];
    println(DeviceName);
    DeviceBtn.setText(DeviceName);
    
  } else {
    selectedColour = colourList.get(kList.getSelection());
    SparkleBtn.setText(kList.getSelection());
  }
    

}
public void saveText(int Index) {
  String[] TextToSave = new String[1]; 
  TextToSave[0] = InputField.getText();
  println("Text to save: " + TextToSave);
  
  switch (Index) {
    
    case 1:
      saveStrings("\\sdcard\\message1.text", TextToSave);
      break;
    case 2:
      saveStrings("\\sdcard\\message2.text", TextToSave);
      break;
    case 3:
      saveStrings("\\sdcard\\message3.text", TextToSave);
      break;
    case 4:
      saveStrings("\\sdcard\\message4.text", TextToSave);
      break;
  }
  
}
public String loadText(int Index) {
  String Message = "";
  String FileName = " ";
  switch (Index) {
    case 1:
      FileName = "\\sdcard\\message1.text";
      break;
    case 2:
      FileName = "\\sdcard\\message2.text";
      break;
    case 3:
      FileName = "\\sdcard\\message3.text";
      break;
    case 4:
      FileName = "\\sdcard\\message4.text";
      break;
    
  }
  try {
    String[] value = loadStrings(FileName);
    Message = value[0];
  }
  catch (NullPointerException e) {
    println("file does not exist");
    Message = " ";
  }
  
  return Message;
  
}

public void StartTimer() {
  savedTime = millis();
}


}
