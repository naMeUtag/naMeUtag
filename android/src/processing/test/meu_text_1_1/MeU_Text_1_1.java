package processing.test.meu_text_1_1;

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

public class MeU_Text_1_1 extends PApplet {

//naMeUtag
//Created by Kingsong Chen, Ross Semenov and Pranav Anand.
	
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
APEditText InputField;
APEditText InputField1;

KetaiList selectionList;

String [] deviceList;
StringDict colourList;
String selectedColour = "FFFFFF";

// timer variables
long savedTime;
int delayValue = 500;

//constants for button width and sizes
//these sizes should fit within Galaxy Note 3 resolution size

final int INPUT_W = 1100;
final int INPUT_H = 300;
final int CTRL_BTN_W = 550;
final int BTN_H = 175;
final int MSG_BTN_W = 590;
//final int SAVE_BTN_W = 260;

//x y positions for buttons
final int Y_INPUT = 250;

final int X_LEFT = 5;
final int X_CMD = 590;
final int X_SAVE = 725;

final int Y_CMD_1 = 1500;
final int Y_CMD_2 = 1050;
final int Y_MSG_1 = 900;
final int Y_MSG_2 = 1125;
final int Y_MSG_3 = 1335;
final int Y_MSG_4 = 1560;

final int Y_STATUS = 80;
final int Y_LABEL = 200;


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
  
  InputField = new APEditText(X_LEFT + 10, 375, INPUT_W, INPUT_H);
  InputField1 = new APEditText(X_LEFT + 10, 960, INPUT_W, INPUT_H);
  
  SendBtn = new APButton(15, Y_CMD_1, CTRL_BTN_W * 2, BTN_H + 250, "Send");
  DeviceBtn = new APButton(X_CMD, Y_CMD_1 - 220, CTRL_BTN_W, BTN_H, "Device");
  ClearBtn = new APButton(15, Y_CMD_1 - 220, CTRL_BTN_W, BTN_H, "Clear Disp");
  //SparkleBtn = new APButton(X_CMD, Y_CMD_2, CTRL_BTN_W, BTN_H, "Colour");

  widgetContainer.addWidget(InputField);
  widgetContainer.addWidget(InputField1);
  widgetContainer.addWidget(SendBtn);
  widgetContainer.addWidget(DeviceBtn);
  widgetContainer.addWidget(ClearBtn);
  //widgetContainer.addWidget(SparkleBtn);
  
  //Load drop down list
  deviceList = bt.list(true); 
  println(deviceList);

}

public void draw() {
  background(0, 0, 0);
  fill(255,255,255);
  textSize(420);
  text("WHO", 45, 370);
  fill(255,255,255);
  textSize(380);
  text("WHAT", 15, 955);

  
  if (remoteAddress.equals("")){
    //fill(255, 0, 0);
    //text("Choose a MeU address.", X_LEFT, Y_STATUS);
  } else {
    if (SendFlag == true) {
      
      if(bt.isConnected() == true) {
        bt.write(MessageToSend);
        //fill(0);
        //text("Message sent!", X_LEFT, Y_STATUS);
        if ((millis() - savedTime) > delayValue) {
          //bt.disconnect();
          SendFlag = false;
        }
        
        
      } else {
        if ((millis() - savedTime) > delayValue) {
          //fill(255,0,0);
          //text ("Cannot connect! Check if MeU is on.", X_LEFT, Y_STATUS);
          
        } else {
          //fill(0);
          //text ("Sending...", X_LEFT, Y_STATUS);
          
        }
      }
    } else {
      //fill(0);
      //text("Type your message: ", X_LEFT, Y_STATUS);
    }
  }
  
}
  
public void onClickWidget(APWidget widget){
  
  if (widget == SendBtn) {
   
    //bt.connect(remoteAddress);
    MessageToSend = "b" + "00FFFF" + "Hi I'm " + InputField.getText() + ',' + ' ' + InputField1.getText() + '\r' + '\n';
    SendFlag = true;
    StartTimer();
    
  } else if (widget == DeviceBtn) {
    deviceList = bt.list(true);
    selectionList = new KetaiList(this, deviceList); 
  } else if (widget == ClearBtn) {
    //bt.connect(remoteAddress);
    MessageToSend = "b" + "00FFFF" + " " + '\r' + '\n';
    SendFlag = true;
    StartTimer();
  
  } else if (widget == SparkleBtn) {
    selectionList = new KetaiList(this, colourList.keyArray());
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
    
    if(bt.isConnected() == true) {
      bt.disconnect();
      DeviceBtn.setText("No Device");
    } else {
      int Attempts = 0;
      while (Attempts < 6) {
        try {
          bt.connect(remoteAddress);
        } catch (Exception ex) {
          println("Trying to connect...");
        }
        if (bt.isConnected() == true) {
          println("Connected");
          break;
        } else {
          Attempts++;
        }
      }
      if (bt.isConnected() == false) {
        fill(255,0,0);
        text ("Cannot connect! Check if MeU is on.", X_LEFT, Y_STATUS);
        DeviceBtn.setText("No Device");
      }
        
    }
    
  } else {
    selectedColour = colourList.get(kList.getSelection());
    SparkleBtn.setText(kList.getSelection());
  }
    

}

public void StartTimer() {
  savedTime = millis();
}


}
