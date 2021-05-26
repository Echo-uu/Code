#include<iostream>
#include<algorithm>
#include<cmath>
#include<vector>

using namespace std;
int T,y;
vector<int>x;
vector<vector<int>> res;
int fun(int n, long long v, vector<int> x, vector<vector<int>> res){
    for(int i=1;i<=n;i++){
        for(int j=1;j<=v;j++){
            if(x[i]>j){
                res[i][j]=res[i-1][j];
            }
            else
                res[i][j]=max(res[i-1][j-x[i]]+x[i],res[i-1][j]);
        }
    }
    return res[n][v];
}

int main(){
    cin>>T>>y;
    int n=1;
    long long all = 0;
    int temp
    while(cin>>temp){
        x.push_back(temp);
        n++;
    }

    long long bag = all-T;
    int ans = fun(n,bag,x,res);
    cout<<ans;
    return 0;
}