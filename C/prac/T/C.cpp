#include<bits/stdc++.h>

using namespace std;
void MergeSort(int a[], int left, int right, int reorder[], int index);
void Merge(int a[], int left, int right, int reorder[], int index);

int main(){
    int n, m, q;
    cin>>n;
    int N=1<<n;
    int a[N], b[N];
    int order[n+1],reorder[n+1];
    memset(order,0,sizeof(order));
    memset(reorder,0,sizeof(reorder));
    for(int i=0;i<N;i++){
        cin>>a[i];
        b[N-i-1]=a[i];
    }
    MergeSort(a,0,N-1,reorder,n);
    MergeSort(b,0,N-1,order,n);
    // for(int i=1;i<=n;i++){
    //     cout<<reorder[i]<<' '<<order[i]<<endl;
    // }
    cin>>m;
    for(int i=1;i<=m;i++){
        cin>>q;
        for(int i=1;i<=q;i++){
            int temp = reorder[i];
            reorder[i] = order[i];
            order[i] = temp;
        }
        int sum=0;
        for(int i=1;i<=n;i++){
            sum+=reorder[i];         
        }
        cout<<sum<<endl;
    }
    return 0;
}

void MergeSort(int a[], int left, int right, int reorder[], int index){
	if(left<right){
		int mid = (left + right) / 2;
		MergeSort(a,left, mid, reorder, index - 1);
		MergeSort(a,mid+1, right, reorder, index - 1);
		Merge(a,left,right, reorder, index);
	}
	else return;
}

void Merge(int a[], int left, int right, int reorder[], int index){
	int mid = (left + right) / 2;
	int lenl = mid - left + 1, lenr = right - mid;
	int l[lenl], r[lenr];
	copy(a+left,a+mid+1,l);
	copy(a+mid+1,a+right+1, r);
	int i = 0, j = 0, k = left, c = 0;
	while(i < lenl && j < lenr){
		if(l[i] < r[j]){
			a[k++] = l[i++];
			
		}
		else{
			a[k++] = r[j++];
            c += lenl - i;
		}
	}
	while(i<lenl)a[k++]=l[i++];
	while(j<lenr)a[k++]=r[j++];
    reorder[index]+=c;
}