import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class showGraphFromSerial extends PApplet {


PrintWriter output;
Serial myPort;
boolean flg_start = false;
String str_format = "myo,val1,val2";
String port = "/dev/cu.usbmodem51327201";
String header = "speed";
float a, b, c;
int k;
float w = 2.0f;
boolean x_val=true,y_val=true,z_val=true;
float yaw,pitch,roll;
float Kp,Kd,Ki;
float graph_max_range = 5000;
graphMonitor testGraph;
float errorIntegral = 0;
boolean errorcountstart;
int start_t;
boolean unfound = true;

public void setup() {
   
   frameRate(120);
   
   testGraph = new graphMonitor("graphTitle", 100, 50, 1000, 400);
   //for (int i = 0;i<Serial.list().length;i++) {
   //  println(Serial.list()[i]);
   //  if (port == Serial.list()[i]) {
   //   unfound = false;
   //  }
   //}
   //if (unfound) exit();
   myPort = new Serial(this, port, 115200);

}

public void draw() {
  background(250);
  serialEvent();
  testGraph.graphDraw(x_val ? (roll) : 0,y_val ? (pitch) : 0,z_val ? (yaw) : 0);
  textSize(30);
  text("Kp : " + nfs(Kp,0,7),1500,100);
  text("Ki : " + nfs(Ki,0,7),1500,150);
  text("Kd : " + nfs(Kd,0,7),1500,200);
  if (errorcountstart) {
    if (millis() - start_t > 5000) {
      errorcountstart = false;
    } else {
      errorIntegral += sqrt((roll - pitch) * (roll - pitch));
    }
  }
  text("error sum : " + errorIntegral,1500,250);
}

public void serialEvent()
{
  int newLine = 13; // new line character in ASCII
  String message;
  do {
    message = myPort.readStringUntil(newLine); // read from port until new line
    if (message != null) {
      String[] list = split(trim(message), ",");
      if (list.length >= 2 && list[0].equals(header)) {
        roll = PApplet.parseFloat(list[1]); // convert to float yaw
        pitch = PApplet.parseFloat(list[2]); // convert to float pitch
        yaw = PApplet.parseFloat(list[3]); // convert to float roll
        Kp = PApplet.parseFloat(list[4])/100000.0f;
        Ki = PApplet.parseFloat(list[5])/100000.0f;
        Kd = PApplet.parseFloat(list[6])/100000.0f;
        // integral = float (list[7]);
      }
    }
  } while (message != null);
}

public void keyPressed() {
  if (keyCode == UP) {
    w = w + 0.1f;
  } else if (keyCode == DOWN) {
    w = w - 0.1f;
  } else if (key == 'x') {
    // x_val = !x_val;
    println(x_val);
  } else if (key == 'y') {
    // y_val = !y_val;
    println(y_val);
  } else if (key == 'z') {
    // z_val = !z_val;
    println(z_val);
  } else if (key == 'e') {
    errorcountstart = true;
    errorIntegral = 0;
    start_t = millis();
  }
  //  else if (key == 'u') {
  //   graph_max_range += 10000;
  // } else if (key == 'd') {
  //   graph_max_range -= 10000;
  // }
  myPort.write(key + "\n");
}

class graphMonitor {
    String TITLE;
    int X_POSITION, Y_POSITION;
    int X_LENGTH, Y_LENGTH;
    float [] y1, y2, y3;
    float maxRange;
    graphMonitor(String _TITLE, int _X_POSITION, int _Y_POSITION, int _X_LENGTH, int _Y_LENGTH) {
      TITLE = _TITLE;
      X_POSITION = _X_POSITION;
      Y_POSITION = _Y_POSITION;
      X_LENGTH   = _X_LENGTH;
      Y_LENGTH   = _Y_LENGTH;
      y1 = new float[X_LENGTH];
      y2 = new float[X_LENGTH];
      y3 = new float[X_LENGTH];
      for (int i = 0; i < X_LENGTH; i++) {
        y1[i] = 0;
        y2[i] = 0;
        y3[i] = 0;
      }
    }

    public void graphDraw(float _y1, float _y2, float _y3) {
      y1[X_LENGTH - 1] = _y1;
      y2[X_LENGTH - 1] = _y2;
      y3[X_LENGTH - 1] = _y3;
      for (int i = 0; i < X_LENGTH - 1; i++) {
        y1[i] = y1[i + 1];
        y2[i] = y2[i + 1];
        y3[i] = y3[i + 1];
      }
      maxRange = 1;
      for (int i = 0; i < X_LENGTH - 1; i++) {
        maxRange = (abs(y1[i]) > maxRange ? abs(y1[i]) : maxRange);
        maxRange = (abs(y2[i]) > maxRange ? abs(y2[i]) : maxRange);
        maxRange = (abs(y3[i]) > maxRange ? abs(y3[i]) : maxRange);
      }
      maxRange = graph_max_range;

      pushMatrix();

      translate(X_POSITION, Y_POSITION);
      fill(240);
      stroke(130);
      strokeWeight(1);
      rect(0, 0, X_LENGTH, Y_LENGTH);
      line(0, Y_LENGTH / 2, X_LENGTH, Y_LENGTH / 2);

      textSize(25);
      fill(60);
      textAlign(LEFT, BOTTOM);
      text(TITLE, 20, -5);
      textSize(22);
      textAlign(RIGHT);
      text(0, -5, Y_LENGTH / 2 + 7);
      text(nf(maxRange, 0, 1), -5, 18);
      text(nf(-1 * maxRange, 0, 1), -5, Y_LENGTH);

      translate(0, Y_LENGTH / 2);
      scale(1, -1);
      strokeWeight(1);
      for (int i = 0; i < X_LENGTH - 1; i++) {
        stroke(255, 0, 0);
        line(i, y1[i] * (Y_LENGTH / 2) / maxRange, i + 1, y1[i + 1] * (Y_LENGTH / 2) / maxRange);
        stroke(255, 0, 255);
        line(i, y2[i] * (Y_LENGTH / 2) / maxRange, i + 1, y2[i + 1] * (Y_LENGTH / 2) / maxRange);
        stroke(0, 0, 0);
        line(i, y3[i] * (Y_LENGTH / 2) / maxRange, i + 1, y3[i + 1] * (Y_LENGTH / 2) / maxRange);
      }
      popMatrix();
    }
}
  public void settings() {  size(1920, 1080, P3D);  smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "showGraphFromSerial" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
