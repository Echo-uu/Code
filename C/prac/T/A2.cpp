#include<bits/stdc++.h>

using namespace std;

int main(){
    string s;
    cin>>s;
    int l, r, m, num;
    string str;
    int i = 0;
    while(i < s.length()){
        if(s[i]==']'){
            r=i;
            l=i;
            while(s[l]!='['){
                if(s[l]=='|'){
                    m=l;
                }
                l--;
            }
            num=stoi(s.substr(l+1,m-l-1));
            str=s.substr(m+1,r-m-1);
            for(int i=1;i<num;i++){
                str+=str;
            }
            s.replace(l,r-l+1,str);
            i=l;
        }
        i++;
    }
    cout<<s;
    return 0;
}
