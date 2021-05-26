#include<iostream>
#include<algorithm>
#include<cmath>
using namespace std;

int n;
struct joke{
    int seq;
    int a;
    int b;
}m[3010];
int ans = 300000;
bool cmp(joke aa, joke bb){
    if(aa.a==bb.a)
        return aa.b<bb.b;
    return aa.a<bb.a;
}



int main(){
    cin>>n;
    for(int i=1;i<=n;i++){
        cin>>m[i].a;
    }
    for(int i=1;i<=n;i++){
        cin>>m[i].b;
        m[i].seq=i;
    }
    sort(m+1,m+n+1,cmp);
    
    for(int i=1;i<=n-2;i++){
        if(m[i].seq>n-2)
            continue;
        for(int j=i+1;j<=n-1;j++){
            if(m[j].seq<m[i].seq)
                continue;
            for(int k=j+1;k<=n;k++){
                if(m[k].seq<m[j].seq)
                    continue;
                ans=min(ans,m[i].b+m[j].b+m[k].b);
            }
        }
    }
    cout<<ans<<endl;
    return 0;
}