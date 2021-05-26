#include<iostream>
#include<algorithm>
#include<cmath>
using namespace std;

double * rgb2yiq(double c[]){
    double R = c[0];
    double G = c[1];
    double B = c[2];

    static double yiq[3];
    yiq[0] = (0.299*R+0.587*G+0.114*B);
    yiq[1] = (0.596*R-0.274*G-0.322*B);
    yiq[2] = (0.212*R-0.523*G+0.311*B);
    for(int i=0; i<=2; i++)
        cout<<yiq[i]<<',';
    cout<<endl;
    return yiq;   
 }

 double * yiq2rgb(float y,float i,float q){
  
  double R = (double)(y+0.956*i+0.621*q);
  double G = (double)(y-0.272*i-0.647*q);
  double B = (double)(y-1.105*i+1.702*q);
  
  if (R<0) R=0;
  if (G<0) G=0;
  if (B<0) B=0;
  if (R>255) R=255;
  if (G>255) G=255;
  if (B>255) B=255;
  double rgb[3];
  rgb[0]=R,rgb[1]=G,rgb[2]=B;
  return rgb;
 }

int main(){
    double rgb[3]={255, 0, 0};
    double rgb1[3]={0, 0, 255};
    double yiqt[3];
    double * yiq, * yiq1;
    
    yiq = rgb2yiq(rgb);
    for(int i=0;i<3;i++)
        yiqt[i]=*(yiq+i);
    yiq1 = rgb2yiq(rgb1);
    double cosrgb = sqrt((rgb[0]-rgb1[0])*(rgb[0]-rgb1[0])+(rgb[1]-rgb1[1])*(rgb[1]-rgb1[1])+(rgb[2]-rgb1[2])*(rgb[2]-rgb1[2]));
    double cosyiq = sqrt((*yiq-yiqt[0])*(*yiq-yiqt[0])+(*(yiq+1)-yiqt[1])*(*(yiq+1)-yiqt[1])+(*(yiq+2)-yiqt[2])*(*(yiq+2)-yiqt[2]));
    cout<<cosrgb<<' '<<cosyiq<<endl;
    return 0;
}