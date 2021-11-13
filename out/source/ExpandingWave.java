import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ExpandingWave extends PApplet {

float speeddiv = 5;
ArrayList<CircleWave> waves;
Recording record;

public void setup() {
    
    background(255);
    waves = new ArrayList<CircleWave>();
    
    CircleWave wave = new CircleWave(width/2, height/2);
    waves.add(wave);
    // wave.display();
    record = new Recording();
    record.start();
    
}
int count = 5;
int counter = 0;

public void draw() {
    background(255);
    for (int i = waves.size()-1; i >= 0; i--){
        CircleWave wave = waves.get(i);
        if (wave.stopped){
            waves.remove(i);
            
        }
    }
    for (CircleWave wave : waves){
        wave.move();
        wave.display();
    }

    if (counter > count && frameCount < 1150 - 400){
        CircleWave wave = new CircleWave(width/2, height/2);
        waves.add(wave);
        counter = 0;
    }
    counter += 1;
    record.control();
}

class CircleWave {
    int segments = 50;
    float radius = 1;
    PVector loc;
    ArrayList<PVector> initial = new ArrayList<PVector>();
    ArrayList<PVector> prevpt = new ArrayList<PVector>();
    ArrayList<PVector> currpt = new ArrayList<PVector>();
    ArrayList<PVector> gravity = new ArrayList<PVector>();
    boolean stopped = false;

    public CircleWave(PVector l){
        loc = l;
        createPoints();
    }
    public CircleWave(float x, float y){
        loc = new PVector(x, y);
        createPoints();
    }

    public void createPoints(){
        for (int i = -1; i < segments; i++){
            float angle = 2*PI*i/segments;
            PVector curr = circlexy(angle, radius);
            currpt.add(curr);
            initial.add(curr);
            PVector force = PVector.sub(curr, loc);
            force.normalize();
            force = PVector.mult(force, random(3, 5));
            PVector prev = PVector.sub(curr, force);
            prevpt.add(prev);
            PVector grav = PVector.div(force, 175);
            gravity.add(grav);
        }
    }

    public void display(){
        // fill(255);
        noFill();
        beginShape();
        for (PVector pt : currpt){
            curveVertex(pt.x, pt.y);
        }
        PVector last1 = currpt.get(1);
        curveVertex(last1.x, last1.y);
        PVector last2 = currpt.get(2);
        curveVertex(last2.x, last2.y);
        endShape(CLOSE);
    }

    public PVector circlexy(float angle, float radius){
        float x = radius * cos(angle) + width/2;
        float y = radius * sin(angle) + height/2;
        PVector loc = new PVector(x, y);
        return loc;
    }

    public void move(){
        for (int i = 0; i < currpt.size(); i++){
            PVector first = initial.get(i);
            PVector curr = currpt.get(i);
            PVector prev = prevpt.get(i);
            PVector grav = gravity.get(i);
            PVector diff = PVector.sub(curr, prev);
            prevpt.set(i, curr);
            curr = PVector.add(curr, diff);
            curr = PVector.sub(curr, grav);

            PVector currloc = PVector.sub(loc, curr);
            PVector prevloc = PVector.sub(loc, prev);
            PVector prevcurr = PVector.sub(curr, prev);
            float dist = currloc.mag() + prevloc.mag();


            if (prevloc.mag() < currloc.mag() && !stopped){
                currpt.set(i, curr);
            } else if (dist <= prevcurr.mag() + 1 && dist >= prevcurr.mag() - 1){
                currpt.set(i, loc);
                stopped = true;
                if (firsttime){
                    println(frameCount);
                    firsttime = false;
                }
                
            } else if (!stopped) {
                currpt.set(i, curr);
            }
        }
    }
}
boolean firsttime = true;

float error = 1;

class Recording {
    boolean recording = false;
    boolean stopped = false;
    int start_frame;
    int stop_frame;
    int frame_rate = 30;
    int recording_time = 40;

    public Recording() {
        
    }

    public void start(){
        if (recording == false && stopped == false) {
                recording = true;
                start_frame = frameCount;
                stop_frame = start_frame + (frame_rate * recording_time);
        }
    }

    public void control(){
        if (recording) {
            saveFrame("output/img-####.png");
            if (stop_frame < frameCount) {
                stopped = true;
                recording = false;
            }
            print(stop_frame, frameCount, '\n');
            if (stopped) {
                println("Finished.");
                System.exit(0);
            }
        }
    }
}
  public void settings() {  size(1080, 1080); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ExpandingWave" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
