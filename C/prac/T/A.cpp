#include<bits/stdc++.h>

using namespace std;
 
int main() {
    stack<int> st;
    string s;
    cin>>s;
    for(int i=0;i<s.length();i++){
        if(s[i]=='['||s[i]=='|'){
            st.push(i);
        }
        if(s[i]==']'){
            int m = st.top(); st.pop();
            int l = st.top(); st.pop();
            int num = stoi(s.substr(l+1,m-1));
            string temp = s.substr(m+1,i-m-1);
            string str;
            for(int j=1;j<=num;j++){
                str+=temp;
            }
            s=s.replace(l,i-l+1,str);
            i=l+str.length()-1;
        }
    }
    cout<<s;
    return 0;
}

