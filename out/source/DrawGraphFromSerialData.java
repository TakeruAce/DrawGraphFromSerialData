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
String port = "/dev/ttyACM0";
// String port = "COM5";
boolean isSerialAvalable = false;
String header = "vector";
float w = 2.0f;
boolean x_val=true,y_val=true,z_val=true;
float Kp,Kd,Ki;
float GRAPH_MAX_RANGE = 100;
int GRAPH_DATA_NUM = 9;
int TIME_RANGE_FOR_GRAPH = 5000;//[millis]
int CORRECT_DATA_NUM = 0;
boolean CORRECT_DATA_NUM_FLAG = false;
ArrayList<ArrayList<Float>> data;
graphMonitor testGraph;
int colorArray[] = {0xff058ED9, 0xff483D3F,0xffE3B44D, 0xffA39A92, 0xff23CE6B, 0xff77685D, 0xffBA6E6E, 0xffDF2935, 0xffDA7422};
ArrayList<Float> slider_amp_ratio;
ControlP5 cp5;
int start_t;
boolean unfound = true;
ControlP5 myTextfield;
String serialtext = "";
ControlP5 serialButton;
float sliderVal0;
float sliderVal1;
float sliderVal2;
float sliderVal3;
float sliderInitialValues[] = {0.01f,0.000f,0,0};
float sliderRange[] = {1.0f,0.05f,0.5f};
int myColor = color(255,0,0);
float signalPrefVal[] = {0,0};

public void setup() {
   
   frameRate(120);
   
   data = new ArrayList<ArrayList<Float>>();
   slider_amp_ratio= new ArrayList<Float>();
   cp5 = new ControlP5(this);
   for (int i =0;i<3;i++) {
      cp5
     .addSlider("sliderVal"+i)
     .setRange(0.0f,sliderRange[i])
     .setPosition(1200,50+50*i)
     .setSize(200,20)
     .setNumberOfTickMarks(101)
     .setValue(sliderInitialValues[i])
     .setColorValueLabel(myColor)
     ; 
   }
   cp5
     .addButton("send",0,1500, 150,100,20);
   testGraph = new graphMonitor("N-channel Serial Plotter", 100, 50, 1000, 400, GRAPH_DATA_NUM,TIME_RANGE_FOR_GRAPH);
   for(int i_data=0;i_data<GRAPH_DATA_NUM;i_data++) {
     slider_amp_ratio.add(0f);
    cp5
    .addSlider("ampslider"+i_data)
    .setRange(-10,10)
    .setPosition(50 + 120* i_data,500)
    .setSize(100,20)
    .setNumberOfTickMarks(201)
    .setValue(slider_amp_ratio.get(i_data))
    .setColorForeground(colorArray[i_data])
    .setColorActive(colorArray[i_data]);

    cp5.addCheckBox("checkbox"+i_data)
    .setPosition(50 + 120 * i_data, 560)
    .setSize(20,20)
    .addItem("data : " + i_data,0)
    .setColorLabels(colorArray[i_data])
    .setColorActive(colorArray[i_data])
    .setColorForeground(colorArray[i_data])
    .activate(0);

    cp5.addCheckBox("reversed"+i_data)
    .setPosition(50 + 120 * i_data, 530)
    .setSize(20,20)
    .addItem("reversed : " + i_data,0)
    .setColorLabels(colorArray[i_data])
    .setColorActive(colorArray[i_data])
    .setColorForeground(colorArray[i_data])
    .activate(0);

   }

  cp5.addTextfield("serialtext")
  .setPosition(1200,50+300)
  .setSize(200,20);
  cp5.addButton("serialsend",0,1500,350,100,20);
  cp5.addButton("graphDataNumReset",0,1500, 300,100,20);

  // for signal generater
  cp5.addButton("signalPrefSend",0,1500, 250,100,20);
  cp5
  .addSlider("signalAmp")
  .setRange(0.0f,10)
  .setPosition(1200,200)
  .setSize(200,20)
  .setNumberOfTickMarks(101)
  .setValue(signalPrefVal[0]);
  cp5
  .addSlider("signalFreq")
  .setRange(-2,2)
  .setPosition(1200,250)
  .setSize(200,20)
  .setNumberOfTickMarks(41)
  .setValue(signalPrefVal[1]);
  cp5
  .addSlider("angularAccRCfilter")
  .setRange(0,1)
  .setPosition(1200,300)
  .setSize(200,20)
  .setNumberOfTickMarks(101)
  .setValue(0);
  
  cp5.addSlider("GRAPHMAXRANGE")
  .setRange(1,5000)
  .setPosition(500,20)
  .setSize(200,20)
  .setNumberOfTickMarks(1000)
  .setValue(GRAPH_MAX_RANGE); 

   cp5.addSlider("TIMERANGE")
  .setRange(1000,5000)
  .setPosition(750,20)
  .setSize(200,20)
  .setNumberOfTickMarks(10)
  .setValue(TIME_RANGE_FOR_GRAPH);

  
  try {
  myPort = new Serial(this, port, 115200);
  println("port connection successed.");
  isSerialAvalable = true;
  } catch (Exception e) {
  isSerialAvalable = false;
  println("port connection failed.");
  }
   

}

public void draw() {
  background(250);
  drawStaticData();
  // if (!data.isEmpty()) {
  //   data.clear();
  // }
  // data = new ArrayList<Float>();
  if(isSerialAvalable) {
     data = serialEvent();
  } else {
    generateDummy();
  }
  // println(data); 
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


public ArrayList<ArrayList<Float>> serialEvent()
{
  int newLine = 13; // new line character in ASCII
  String message;
  ArrayList<ArrayList<Float>> return_data = new ArrayList<ArrayList<Float>>();
  do {
    ArrayList<Float> input_data = new ArrayList<Float>();
    message = myPort.readStringUntil(newLine); // read from port until new line
    if (message != null) {
      String[] list = split(trim(message), ",");
      if (!CORRECT_DATA_NUM_FLAG || list[0].equals(header) && list.length == CORRECT_DATA_NUM + 1) {
        for (int i_input=0; i_input < min(list.length - 1,GRAPH_DATA_NUM) ;i_input++) {
          // data[i_input] = float(list[i_input+1]);
          input_data.add(PApplet.parseFloat(list[i_input+1]));
        }
      }
    }
    if (!CORRECT_DATA_NUM_FLAG && input_data.size() > 0) {
      CORRECT_DATA_NUM = input_data.size();
      CORRECT_DATA_NUM_FLAG = true;
    }
    if (input_data.size() > 0) return_data.add(input_data);
  } while (message != null);
  return return_data;
}

public void keyPressed() {
  if (keyCode == UP) {
    w = w + 0.1f;
  } 
  if (key == 'S') {
    saveFrameAsPNG();
  }
  myPort.write(key + "\n");
}

class graphMonitor {
    String TITLE;
    int X_POSITION, Y_POSITION;
    int X_LENGTH, Y_LENGTH;
    int DATA_NUM;
    int TIME_RANGE;
    ArrayList<ArrayList<Float>> y = new ArrayList<ArrayList<Float>>();
    // float [][] y;
    float maxRange;
    graphMonitor(String _TITLE, int _X_POSITION, int _Y_POSITION, int _X_LENGTH, int _Y_LENGTH, int _DATA_NUM, int _TIME_RANGE) {
      TITLE = _TITLE;
      X_POSITION = _X_POSITION;
      Y_POSITION = _Y_POSITION;
      X_LENGTH   = _X_LENGTH;
      Y_LENGTH   = _Y_LENGTH;
      DATA_NUM = _DATA_NUM;
      TIME_RANGE = _TIME_RANGE;
    }

    public void graphDrawForArray(ArrayList<ArrayList<Float>> new_data) {
      //New Dataを追加
      if (new_data.size() > 0 && new_data.get(0).size() > 0) {
        y.addAll(new_data);
      }
      // data内に不要な時間軸のdataがあれば削除する(タイムスタンプは昇順になっているはず)
      // println(y);
      // println(y.size());
      if (y.size() < 1 || y.get(0).size() < 1) {
        return;
      } else {
        int currentTime = y.get(y.size()-1).get(0).intValue();
        for (int i_y = 0;i_y<y.size();i_y++) {
          if (currentTime - y.get(i_y).get(0) > TIME_RANGE) {
            y.remove(i_y);
          } else {
            break;
          }
        }
      }
      maxRange = cp5.getController("GRAPHMAXRANGE").getValue();
      TIME_RANGE = (int)cp5.getController("TIMERANGE").getValue();

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

      for(int i = 0;i<20;i++) {
        line(X_LENGTH/20*i , 0 , X_LENGTH/20*i ,Y_LENGTH);
      }

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
      text("0[s]", 5, Y_LENGTH + 25);
      text(TIME_RANGE + "[s]",X_LENGTH, Y_LENGTH +25);


      translate(0, Y_LENGTH / 2);
      scale(1, -1);
      strokeWeight(1);
      int oldestTime = y.get(0).get(0).intValue();
      for (int i_data = 1;i_data < y.get(y.size()-1).size();i_data++) {
        for (int i_t=0;i_t<y.size()-1;i_t++) {
          if ((int)cp5.getGroup("checkbox"+(i_data-1)).getController("data : " + (i_data-1)).getValue() == 1) {
            stroke(colorArray[i_data-1]);
            line((y.get(i_t).get(0) - oldestTime) / TIME_RANGE * X_LENGTH ,
                  y.get(i_t).get(i_data)
                   * (Y_LENGTH / 2) / maxRange * (float)Math.pow(10, cp5.getController("ampslider"+(i_data-1)).getValue())
                   * ((int)cp5.getGroup("reversed"+(i_data-1)).getController("reversed : " + (i_data-1)).getValue() == 1 ? 1 : -1),
                 (y.get(i_t+1).get(0) - oldestTime) / TIME_RANGE * X_LENGTH, 
                  y.get(i_t+1).get(i_data) * (Y_LENGTH / 2) / maxRange
                  *  (float)Math.pow(10, cp5.getController("ampslider"+(i_data-1)).getValue()) 
                  * ((int)cp5.getGroup("reversed"+(i_data-1)).getController("reversed : " + (i_data-1)).getValue() == 1 ? 1 : -1)) ;
          }
        }
      }

      popMatrix();
    }

    public void setTimeRange(int new_time_range) {
      TIME_RANGE = new_time_range;
    }
}

public void send() {
  myPort.write("C1" + String.format("%.10f",sliderVal0) + "\n");
  myPort.write("C2" + String.format("%.10f",sliderVal1) + "\n");
  myPort.write("C3" + String.format("%.10f",sliderVal2) + "\n");
  // myPort.write("t" + String.format("%.10f",sliderVal3) + "\n");
  println("C1:" + String.format("%.10f",sliderVal0) + "\n");
}

public void serialsend() {
  myPort.write(serialtext + "\n");
}

public void drawStaticData () {

}

public void generateDummy() {
  ArrayList<Float> dummy = new ArrayList<Float>();
  dummy.add((float)millis());
  for (int i_data = 0;i_data<GRAPH_DATA_NUM;i_data++) {
    dummy.add(cp5.getController("ampslider"+i_data).getValue() * sin(2*PI*0.5f*(millis()/1000.0f - (1.0f / GRAPH_DATA_NUM * i_data) * 2.0f)));
  }
  data.add(dummy);
}

public void graphDataNumReset() {
  CORRECT_DATA_NUM_FLAG = false;
}

public void signalPrefSend() {
  myPort.write("S1" + String.format("%.10f",cp5.getController("signalAmp").getValue()) + "\n");
  myPort.write("S2" + String.format("%.10f",Math.pow(10,cp5.getController("signalFreq").getValue())) + "\n");
  myPort.write("S3" + String.format("%.10f",cp5.getController("angularAccRCfilter").getValue()) + "\n");
  println(cp5.getController("signalAmp").getValue());
  println(Math.pow(10,cp5.getController("signalFreq").getValue()));
  }

  public void saveFrameAsPNG() {
    PImage saveImage = get(0, 0, 1200, 500);
    saveImage.save("data/screenshot"+ month() +"-" + day() +"-"+ hour() + "-" + minute() + "-" + second() +".png");
  }
  public void settings() {  size(1700, 700, P3D);  smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "DrawGraphFromSerialData" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
