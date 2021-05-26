#include<iostream>
#include<algorithm>
using namespace std;



int main(){
    int n;
    int a[200010];

    cin>>n;
    for(int i=1;i<=n;i++){
        cin>>a[i];
    }
    sort(a+1,a+1+n);

    int cnt=0,ans=0,flgpos=0;
    bool flag=false;

    for(int i=1;i<=n;i++){
        for(int i=1;i<=n;i++){
            if(a[i]==i*i){
                cnt++;
            }
            else if(abs(a[i]-i*i)<=3){
                cnt++;
                ans+=abs(a[i]-i*i);
            }
            if(cnt>=n/2){
                cout<< ans<<endl;
                break;
            }
        }
        if(cnt>=n/2)
            break;
    }


    return 0;
}