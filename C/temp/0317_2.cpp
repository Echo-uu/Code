#include<iostream>
#include<algorithm>
using namespace std;

int n;
long long ans=0;

struct node{
    int val;
    int mod;
}nums[100010];

bool cmp(node a, node b){
    if(a.mod == b.mod)
        return a.val<b.val;
    return a.mod<b.mod;
}

int main(){
    cin>>n;
    for(int i=1;i<=n;i++){
        cin>>nums[i].val;
        ans+=nums[i].val;
        nums[i].mod = nums[i].val % 6;        
    }
    sort(nums+1,nums+n+1,cmp);
    
    int diff = ans % 6;
    if(diff==0)cout<<ans;
    else{
        for(int i=1;i<=n;i++){
            if(diff==nums[i].mod){
                ans-=nums[i].val;
                break;
            }
        }
        if(ans%6==0)cout<<ans;
        else cout<<-1;
    }
    return 0;
}