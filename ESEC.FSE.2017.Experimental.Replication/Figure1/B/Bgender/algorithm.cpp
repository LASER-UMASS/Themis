#include <iostream>                  // for std::cout
#include <fstream>
#include <utility>
#include<vector>
#include<string>
#include<math.h>
#include <ios>
#include <stdlib.h>
#include <time.h>
using namespace std;

typedef pair<int,int> disc_attr;//pair of attribute index and the value of that attribute which is being discriminated
vector< vector<int> > getAllSubsets_length_i(vector<vector<int> > set, int k);


class Algorithm {
    int n, t;//n is the size of input
             // t is the number of values an attribute can take
    bool discriminating;
            //ground truth that algorithm is discriminating or not
    int k;
        //size of discrimination if it is dircriminating
    vector<disc_attr> disc_index; //This vector contains the indices and attribute values  of the input attributes which are discriminating.
    
public:
    void initialise (int,int);
    void set_discrimination (bool,int);
    void print_algo();
    void print_algo_to_file(string);
    void read_algo(string);
    int area() {return n*t;}
    bool algorithm_output(vector<int>);
    int num_steps_naive (vector<int>);
    bool check_adamant (vector<int>, vector<int> , int &);
    bool check_XX (vector<int>, vector<int> , int &);
    vector<int> create_random_input();
    int num_steps_heuristic (vector<int> input);
    bool check_XX_complete (vector<int>, vector<int> , int &);


   
};

void Algorithm::initialise (int x, int y) {
    n = x;
    t = y;
}

vector<int> Algorithm::create_random_input () {
    vector<int> temp;
    for(int i=0;i<n;i++){
        int tmp = rand()%t;
        temp.push_back(tmp);
    }
    return temp;
}

void Algorithm::set_discrimination(bool disc, int size){
    
    discriminating = disc;
    if(!discriminating){
        k=0;
        
        return;
    }
    k = size;
    
    if(size>n){
        discriminating=false;
        k=0;
        return;
    }
    vector<int> attr;
    
    for(int i=0;i<k;i++){
        disc_attr temp;
        int attr_index  = rand() % n;
        while(find(attr.begin(), attr.end(),attr_index)!=attr.end()){
            attr_index = rand()%n;
        }
        attr.push_back(attr_index);
        temp.first = attr_index;
        temp.second = rand() % t;
        disc_index.push_back(temp);
        
    }
    
}
void Algorithm::print_algo(){
    cout<<"n is "<<n<<" t is "<<t<<endl;
    cout<<"discriminating = "<<discriminating<<endl;
    for(int i=0;i<disc_index.size();i++){
            cout<<"attribute number "<<disc_index[i].first<<" attribute value "<<disc_index[i].second<<endl;
    }

}
void Algorithm::print_algo_to_file(string s){
    
    fstream outStream;
    outStream.open(s,ios::out);
    
    outStream<<n<<" "<<t<<endl;
    outStream<<discriminating<<endl;
    outStream<<k<<endl;
    for(int i=0;i<disc_index.size();i++){
        outStream<<disc_index[i].first<<" "<<disc_index[i].second<<endl;
    }
    outStream.close();

}
void Algorithm::read_algo(string s){
    fstream inStream;
    inStream.open(s,ios::in);
    
    inStream>>n>>t;
    inStream>>discriminating;
    inStream>>k;
    if(k>0)
    for(int i=0;i<k;i++){
        disc_attr temp;
        inStream>>temp.first>>temp.second;
        disc_index.push_back(temp);
    }

    
}

vector< vector<int> > getAllSubsets(vector<int> set)
{
    vector< vector<int> > subset;
    vector<int> empty;
    subset.push_back( empty );
    
    for (int i = 0; i < set.size(); i++)
    {
        vector< vector<int> > subsetTemp = subset;
        
        for (int j = 0; j < subsetTemp.size(); j++)
            subsetTemp[j].push_back( set[i] );
        
        for (int j = 0; j < subsetTemp.size(); j++)
            subset.push_back( subsetTemp[j] );
    }
    return subset;
}


vector< vector<int> > getAllSubsets_length_i(vector<vector<int> > set, int k)
{
    vector< vector<int> > subset;
    
    for (int i = 0; i < set.size(); i++)
    {
        vector<int> temp = set[i];
        if(temp.size()==k)
            subset.push_back(set[i]);
    }
    return subset;
}

vector<int> convert(int number,int base){
    
    vector<int> temp;
    if(number == 0 ){
        temp.push_back(0);
        return temp;
    }
    
    
    temp.push_back(number % base);
    vector<int> temp1 = convert(number / base, base);
    
    temp.insert( temp.end(), temp1.begin(), temp1.end() );
    return temp;
    
}
void print_vect(vector<int> vec){
    for(int i=0;i<vec.size();i++)
        cout<<vec[i]<<",";
    cout<<"\n";
    
}

vector<vector<int> > generate_input (int n, int t){
    
    int i,j;
    vector<vector<int> > inputs;
    for (i=0;i<pow(t,n);i++)//loop through permutations
    {
        vector<int> out = convert(i,t);
        out.pop_back();
        
        if(out.size()<n){
            vector<int> temp(n-out.size(),0);
            out.insert( out.end(), temp.begin(), temp.end() );
            
        }
        inputs.push_back(out);
    }
    return inputs;
    
}

bool Algorithm::check_adamant (vector<int>input, vector<int> pattern, int &query){
    //cout<<"new call";
    //print_vect(pattern);
    //print_vect(input);
    //cout<<query<<endl;
    
    int i=0;
    bool input_algo_output = algorithm_output(input);
    vector<vector<int> > inputs;
    inputs = generate_input(n,t);
    //cout<<query<<endl;
    for (i=0;i<inputs.size();i++){
        int j=0;

        for( j=0;j<pattern.size();j++){
            if(inputs[i][pattern[j]] != input[pattern[j]])
                break;
        }
        if(j==pattern.size()){
            //get output on this input
            //print_vect(inputs[i]);
            bool temp_out = algorithm_output(inputs[i]);
            query++;
            if(temp_out != input_algo_output)
                return false;
        }
            
    }
    return true;
    
}

vector<int> get_complement(int n,vector<int> a){
    
    int i=0;
    vector<int> temp;
    int j=0;
    for( j=0;j<a.size();){
        if(a[j]==i){
            j++;
            i++;
        }
        else if(a[j]>i){
            temp.push_back(i);
            i++;
        }
        else if(a[j]<i){
            cout<<"*******************PROBLEM**********************"<<endl;
        }
    }
    if(j==a.size() && i<n){
        for(;i<n;i++){
            temp.push_back(i);
        }
        
    }
    return temp;
}

bool Algorithm::check_XX (vector<int>input, vector<int> pattern, int &query){
    
    int i=0;
    bool input_algo_output = algorithm_output(input);
    vector<vector<int> > inputs;
    inputs = generate_input(n,t);
    //print_vect(pattern);
    pattern = get_complement(n,pattern);
    //cout<<query<<endl;

    for (i=0;i<inputs.size();i++){
        int j=0;
        
        for( j=0;j<pattern.size();j++){
            if(inputs[i][pattern[j]] != input[pattern[j]]){
                break;
            }
        }
        if(j==pattern.size()){
            //get output on this input
            //print_vect(inputs[i]);
            if(input!=inputs[i]){
                bool temp_out = algorithm_output(inputs[i]);
                //cout<<query<<endl;
                query++;
                if(temp_out == input_algo_output && input!=inputs[i])
                    return false;
            }
        }
        
    }
    return true;
    
}

//This is the naive which does not store anything
// We can make another naive which stores the output on whole possible set of inputs and the time will become half but we need to compare our output on same memory so this is fine to start with
int Algorithm::num_steps_naive (vector<int> input){
    
    vector< vector<int> > patternSet;
    
    vector<int> pattern_ind;
    for(int i=0;i<n;i++){
        pattern_ind.push_back(i);
    }
    
    patternSet = getAllSubsets (pattern_ind);
    int query = 0;
    cout<<"power set "<<patternSet.size()<<endl;
    int i=0;
    for(i=1;i<patternSet.size();i++){
        if (check_XX(input,patternSet[i], query)){
            if( check_adamant(input,patternSet[i], query))
            {
                
            cout<<"queries "<<query<<endl;
            cout<<"Pattern length "<<patternSet[i].size()<<endl;
            print_vect(patternSet[i]);
            return query;
            }
        }
        //print_vect(patternSet[i]);
        
    }
        //Get the pattern of this length
        //check if it is adamant for this attribute
        //check if it is XX for this attribute
    cout<<"queries "<<query<<endl;
    return query;
}

bool isSubset (vector<int> A, vector<int> B){//Checks if A is a subset of B
    
    sort(A.begin(),A.end());
    sort(B.begin(),B.end());
    int b_ptr=0;
    int i=0;
    for( i=0;i<A.size();){
        if(A[i]>B[b_ptr])
            b_ptr++;
        else if(A[i]==B[b_ptr]){
            i++;
            b_ptr++;
        }
        else if ( A[i] < B[b_ptr])
            break;
        if(b_ptr==B.size() )
            break;
    }
    if(i==A.size())
        return true;
    else return false;
    
}


bool Algorithm::check_XX_complete (vector<int>input, vector<int> pattern, int &query){
    
    int i=0;
    bool input_algo_output = algorithm_output(input);
    vector<vector<int> > inputs;
    inputs = generate_input(n,t);
    vector<int> pattern_comp = get_complement(n,pattern);
    //cout<<"Query "<<query<<endl;
    //print_vect(pattern);
    //print_vect(pattern_comp);
    for (i=0;i<inputs.size();i++){
        int j=0;
        
        for( j=0;j<pattern_comp.size();j++){
            if(inputs[i][pattern_comp[j]] != input[pattern_comp[j]])
                break;
        }
        if(j==pattern_comp.size()){
            //get output on this input
            
            int j=0;
            
            for( j=0;j<pattern.size();j++){
                if(inputs[i][pattern[j]] == input[pattern[j]])
                    break;
            }
            if(j==pattern.size())
                if(input!=inputs[i]){
                    //print_vect(inputs[i]);
                    bool temp_out = algorithm_output(inputs[i]);
                    query++;
                    if(temp_out == input_algo_output && input!=inputs[i])
                        return false;
                }
        }
        
    }
    return true;
    
}


int Algorithm::num_steps_heuristic (vector<int> input){
    
    vector< vector<int> > patternSet;
    
    vector<int> pattern_ind;
    for(int i=0;i<n;i++){
        pattern_ind.push_back(i);
    }
    
    patternSet = getAllSubsets (pattern_ind);//Gets power set of all possible patterns of all lengths
    cout<<"power set "<<patternSet.size()<<endl;
    //getAllSubsets_length_i
    
    vector< vector<int> > X;
    int query = 0;
    
    for (int i=1;i<=n;i++){
        //For each i get the set of patterns through getAllSubsets_length_i
        vector< vector<int> > patterns_i;
        patterns_i = getAllSubsets_length_i(patternSet,i );
        {
            for(int j=0;j<patterns_i.size();j++){
                int k=0;

                for (k=0;k<X.size();k++){
                    if(isSubset(X[k],patterns_i[j])){
                            X.push_back(patterns_i[j]);
                            //print_vect(X[0]);
                            //print_vect(patterns_i[j]);
                            //print_vect(X[k]);
                            //cout<<k<<endl;
                            break;
                    }
                }

                if(k==X.size()){
                    if(!check_XX_complete(input,patterns_i[j], query)){
                        
                        X.push_back(patterns_i[j]);
                        
                    }
                        //print_vect(X[0]);
                    //print_vect(patterns_i[j]);
                }


            }
        }
        //Now for each pattern we run check_XX_complete if all elements of X are not the subset of this pattern
        //If true good
        //if false update in X
    }
    cout<<query<<endl;
    cout<<X.size()<<endl;
    
    for (int i=n;i>0;i--){
        //For each i get the set of patterns through getAllSubsets_length_i
        vector< vector<int> > patterns_i;
        patterns_i = getAllSubsets_length_i(patternSet,i );
        {
            for(int j=0;j<patterns_i.size();j++){
                int k=0;
                for (k=0;k<X.size();k++){
                    if(isSubset(X[k],patterns_i[j]) && isSubset(patterns_i[j], X[k])){
                        //print_vect(X[k]);
                        //print_vect(patterns_i[j]);
                        break;
                    }
                }
                
                if(k==X.size()){
                    if(!check_adamant(input,patterns_i[j], query))
                    {
                     X.push_back(patterns_i[j]);
 //                        print_vect(patterns_i[j]);
                    }
                    else{
                        cout<<true;
                        cout<<"Pattern length "<<patterns_i[j].size()<<endl;
                        cout<<"Query "<<query<<endl;
                        return query;
                    }
                        
                }
                
                
            }
        }
        //Now for each pattern we run check_XX_complete if all elements of X are not the subset of this pattern
        //If true good
        //if false update in X
    }
    
    cout<<"queries "<<query<<endl;

    
    //TILL NOW I have got all sets which are in X
    //Now I need to check for adamant property
    //How?? Check for each set which is not in X adamant property....
    return query;

}





bool Algorithm::algorithm_output(vector<int> input){
    int i=-100;
    if(!discriminating){
        return (rand()%3<2);
    }
    for( i=0;i<k;i++){
        if(input[disc_index[i].first]!=disc_index[i].second)
            break;
    }
    if(i==k)
        return false;
    return true;//rand()%2;
    
}

int main (int argc, char *argv[]) {
    
    srand (time(NULL));

    if( argc != 6 ) {
        cout<<"USAGE ./a.out <SIZE OF INPUT> <NUMBER OF ATTRIBUTE VALUES> <DISCRIMINATING> <LENGTH OF L> <FILE>"<<endl;
        cout<<" IF READ ALGO FROM FILE PUT INITIAL PARAMETERS AS 0"<<endl;
        return 0;
    }
    
    int n = stoi(argv[1]);//size of input
    int k = stoi(argv[2]);//different number of values an attribute can take
    Algorithm blackbox;
    if(n>0 && k>0){
        blackbox.initialise (n,k);
        blackbox.set_discrimination(stoi(argv[3]),stoi(argv[4]));
        blackbox.print_algo();
        blackbox.print_algo_to_file(argv[5]);
        
    }
    else{
        blackbox.read_algo(argv[5]);
        blackbox.print_algo();
    }
    
    
    int naive_num, heuristic_num;
    int num_iter = 100;
    for(int i=0;i<num_iter;i++){
        vector<int> input = blackbox.create_random_input();
        print_vect(input);
        cout<<i<<endl;
        
        cout<<blackbox.algorithm_output(input);
        naive_num+=(blackbox.num_steps_naive(input));
        heuristic_num+=(blackbox.num_steps_heuristic(input));
        
        
    }
    cout<<naive_num/num_iter<<endl;
    cout<<heuristic_num/num_iter<<endl;
    
    fstream outStream;
    outStream.open("Score",ios::app);
    
    outStream<<n<<"\t"<<naive_num/num_iter<<"\t"<<heuristic_num/num_iter<<endl;
    outStream.close();
    
    //for(int k=0;k<5;k++)
    //    input.push_back(3);
    return 0;
}
