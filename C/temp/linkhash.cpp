#include<iostream>

using namespace std;

class node{
public:
    int val;
    int key;
    node* next;

    node(int key, int val):key(key), val(val), next(NULL){}
};

class hashmap{
private:
    int tablesize;
    node* hashtable[];
public:
    hashmap(){
        tablesize = 1000;
        for(int i=0; i<tablesize; i++){
            hashtable[i]=NULL;
        }
    }

    void put(int key, int value){
        int index = key % 100;
        node* np = new node(key, value);
        if(np == NULL){
            return;
        }
        np->next = hashtable[index];
        hashtable[index] = np;
    }

    int get(int key){
        int index = key % 100;
        node* np = hashtable[index];
        while(np != NULL){
            if(np->key == key){
                return np->val;
            }
            np = np->next;
        }
        return -1;
    }

    void remove(int key){
        int index = key % 100;
        node* np = hashtable[index];
        node* prev = NULL;
        if(!np){
            return;
        }
        while(np){
            if(np->key == key){
                if(prev == NULL){
                    hashtable[index] = np->next;
                }
                else{
                    prev->next = np->next;
                }
                delete np;
                break;
            }
            prev = np;
            np = np->next;
        }
    }
};

int main(){
    hashmap* obj = new hashmap();
    obj->put(1,1);
    obj->put(2,4);
    obj->put(3,5);

    int ans = obj->get(1);
    cout<<ans<<endl;

    obj->remove(1);
    cout<<obj->get(1)<<endl;
    cout<<obj->get(3)<<endl;
    return 0;
}