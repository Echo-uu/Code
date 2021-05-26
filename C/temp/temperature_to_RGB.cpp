#include<iostream>
#include<cmath>

using namespace std;


double * colorTemperature_to_RGB(double colorTemp){
    double r, g, b;
    double * tmp = new double[4];
    double temp = colorTemp / 100;
    if(temp <= 66)
        r = 255;
    else{
        r = temp - 60;
        r = 329.698727446 * (pow(r,(-0.1332047592)));
        if(r < 0) 
            r = 0;
        if(r > 255)
            r = 255;
    }

    if(temp <= 66){
        g = temp; 
        g = 99.4708025861 * log(g) - 161.1195681661; 
        if(g < 0) 
            g = 0; 
        if(g > 255)
            g = 255; 
    }
    else{
        g = temp - 60;
        g = 288.1221695283 * (pow(g, -0.0755148492)); 
        if(g < 0)  g = 0;
        if(g > 255)  g = 255; 
    }

    if (temp >= 66)  
        b = 255;
    else{ 
        if(temp <= 19)  
            b = 0;
        else{ 
            b = temp - 10;
            b = 138.5177342231 * log(b) - 305.0447927307;
            if (b < 0)  b = 0; 
            if (b > 255)  b = 255; 
        }
    }
    tmp[1] = r, tmp[2] = g, tmp[3] = b;
    return tmp;
}


int main(){
    double colorTemp[11]={0, 4700, 4704.166667, 4708.333333, 4712.5, 4712.5, 4712.5, 4712.5, 4708.333333, 4704.166667, 4700};
    
    for(int i=1; i<=10; i++){
        double * tmp = colorTemperature_to_RGB(colorTemp[i]);
        cout<<tmp[3]<<endl;
        //cout<<tmp[1]<<','<<tmp[2]<<','<<tmp[3]<<endl;
    }
    return 0;
}
