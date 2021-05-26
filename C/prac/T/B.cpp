#include<bits/stdc++.h>

using namespace std;

int main(){
    int n;
    int sz[100010];
    cin>>n;
    stack<int> st1,st2;
    vector<int> v1,v2;
    for(int i=1;i<=n;i++)cin>>sz[i];
    
    for(int i=1,j=n;i<=n,j>0;i++,j--){
        v1.push_back(st1.size());
        v2.push_back(st2.size());
        
        while(!st1.empty()&&st1.top()<=sz[i])st1.pop();
        while(!st2.empty()&&st2.top()<=sz[j])st2.pop();
        
        st1.push(sz[i]);
        st2.push(sz[j]);
    }
    reverse(v2.begin(),v2.end());
    for(int i=0;i<n;i++)cout<<v1[i]+v2[i]+1<<' ';
    return 0;
}