import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 
import controlP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class DrawGraphFromSerialData extends PApplet {



Serial myPort;
boolean flg_start = false;
String str_format = "myo,val1,val2";
String port = "/dev/cu.usbmodem64315601";
String header = "vector";
float INPUT_DATA_NUM = 6;
float w = 2.0f;
boolean x_val=true,y_val=true,z_val=true;
float input,target,output;//for general pid
float Kp,Kd,Ki;
float GRAPH_MAX_RANGE = 500;
int GRAPH_DATA_NUM = 3;
float data[];
graphMonitor testGraph;
int colorArray[] = {0xff058ED9, 0xff483D3F,0xffE3B44D, 0xffA39A92, 0xff23CE6B, 0xff77685D, 0xffBA6E6E, 0xffDF2935,0xffDA7422};
float errorIntegral = 0;
boolean errorcountstart;
int start_t;
boolean unfound = true;
ControlP5 mySliders[] = new ControlP5[4];
ControlP5 myButtons[] = new ControlP5[4];
ControlP5 myTextfield;
String serialtext = "";
ControlP5 serialButton;
float sliderVal0;
float sliderVal1;
float sliderVal2;
float sliderVal3;
float sliderInitialValues[] = {0.1f,0.000f,0,0};
int myColor = color(255,0,0);
public void setup() {
   
   frameRate(120);
   
   for (int i =0;i<3;i++) {
     mySliders[i] = new ControlP5(this);
     mySliders[i]
     .addSlider("sliderVal"+i)
     .setRange(0.0f,1.0f)
     .setPosition(1200,50+50*i)
     .setSize(200,20)
     .setNumberOfTickMarks(101)
     .setValue(sliderInitialValues[i])
     .setColorValueLabel(myColor); //現在の数値の色

     myButtons[i] = new ControlP5(this);
     myButtons[i].addButton("send",0,1500,50 + 50*i,100,20);
   }
   mySliders[3] = new ControlP5(this);
   mySliders[3].addSlider("sliderVal"+3).setRange(0,1023)
     .setPosition(1200,50+50*3)
     .setSize(200,20)
     .setNumberOfTickMarks(1024)
     .setValue(sliderInitialValues[3]);
   testGraph = new graphMonitor("graphTitle", 100, 50, 1000, 400, GRAPH_DATA_NUM);
   myTextfield = new ControlP5(this);
   myTextfield.addTextfield("serialtext")
   .setPosition(1200,50+50*5)
   .setSize(200,20);
   serialButton = new ControlP5(this);
   serialButton.addButton("serialsend",0,1500,50 + 50*5,100,20);
   myPort = new Serial(this, port, 115200);
   print(myProt);
   data = new float[GRAPH_DATA_NUM];
}

public void draw() {
  background(250);
  serialEvent();
  testGraph.graphDrawForArray(data);
  textSize(18);
  text(nfs(sliderVal0,0,7),1200 + 200,100-8);
  text(nfs(sliderVal1,0,7),1200 + 200,150-8);
  text(nfs(sliderVal2,0,7),1200 + 200,200-8);
  text(serialtext,1200,500);
  //if (errorcountstart) {
  //  if (millis() - start_t > 5000) {
  //    errorcountstart = false;
  //  } else {
  //    errorIntegral += sqrt((roll - pitch) * (roll - pitch));
  //  }
  //}
  //text("error sum : " + errorIntegral,1500,250);
}


public void serialEvent()
{
  int newLine = 13; // new line character in ASCII
  String message;
  do {
    message = myPort.readStringUntil(newLine); // read from port until new line
    if (message != null) {
      String[] list = split(trim(message), ",");
      if (list.length == INPUT_DATA_NUM + 1 && list[0].equals(header)) {
        for (int i_input=0; i_input < min(list.length,GRAPH_DATA_NUM) ;i_input++) {
          data[i_input] = PApplet.parseFloat(list[i_input+1]) ;
        }
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
    x_val = !x_val;
    println(x_val);
  } else if (key == 'y') {
    y_val = !y_val;
    println(y_val);
  } else if (key == 'z') {
    z_val = !z_val;
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
    int DATA_NUM;
    float [][] y;
    float maxRange;
    graphMonitor(String _TITLE, int _X_POSITION, int _Y_POSITION, int _X_LENGTH, int _Y_LENGTH, int _DATA_NUM) {
      TITLE = _TITLE;
      X_POSITION = _X_POSITION;
      Y_POSITION = _Y_POSITION;
      X_LENGTH   = _X_LENGTH;
      Y_LENGTH   = _Y_LENGTH;
      DATA_NUM = _DATA_NUM;
      y = new float[DATA_NUM][X_LENGTH];
      // データの初期化
      for (int i_y = 0;i_y<DATA_NUM;i_y++) {
        for (int i_data = 0; i_data < X_LENGTH; i_data++) {
          y[i_y][i_data] = 0;
        }
      }
    }

    public void graphDrawForArray(float new_data[]) {
      // データのアップデート
      for (int i_y = 0;i_y<DATA_NUM;i_y++) {
        y[i_y][X_LENGTH-1] = new_data[i_y];
    
        for (int i_data = 0; i_data < X_LENGTH - 1; i_data++) {
          y[i_y][i_data] = y[i_y][i_data + 1];
        }
      }
      
      maxRange = GRAPH_MAX_RANGE;

      pushMatrix();

      translate(X_POSITION, Y_POSITION);
      fill(240);
      stroke(130);
      strokeWeight(1);
      rect(0, 0, X_LENGTH, Y_LENGTH);
      line(0, Y_LENGTH / 2, X_LENGTH, Y_LENGTH / 2);
      stroke(200);
      line(0, Y_LENGTH / 4, X_LENGTH, Y_LENGTH / 4);
      line(0, Y_LENGTH * 3 / 4, X_LENGTH, Y_LENGTH * 3 / 4);
      stroke(130);

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
      for (int i_data = 0; i_data < X_LENGTH - 1; i_data++) {
        for (int i_y = 0;i_y<DATA_NUM;i_y++) {
          stroke(colorArray[i_y]);
          // need to line current and past 'time' for precious graph 
          line(i_data, y[i_y][i_data] * (Y_LENGTH / 2) / maxRange, i_data + 1, y[i_y][i_data + 1] * (Y_LENGTH / 2) / maxRange);
         }
      }
      popMatrix();
    }
}

public void send() {
  myPort.write("C1" + String.format("%.10f",sliderVal0) + "\n");
  myPort.write("C2" + String.format("%.10f",sliderVal1) + "\n");
  myPort.write("C3" + String.format("%.10f",sliderVal2) + "\n");
  // myPort.write("t" + String.format("%.10f",sliderVal3) + "\n");
  println("C1" + String.format("%.10f",sliderVal0) + "\n");
}

public void serialsend() {
  myPort.write(serialtext + "\n");
}
  public void settings() {  size(1500, 700, P3D);  smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "DrawGraphFromSerialData" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
