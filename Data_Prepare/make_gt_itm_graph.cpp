#include <iostream>
#include <cstdio>
#include <algorithm>
#include <fstream>

using namespace std;

const int MAX_N = 650;
const int MAXL = 1000;
const int MAX_PATH_LENGTH = (1<<29);

const int TS = 0;
const int RAND = 1;

const int TREE = 0;
const int ONEWAY = 1;
const int TWOWAY = 2;

const int TOPO = 1;
const int ROUTES = 2;

// Store Node Information
struct record {
  bool isT;
  int tdomainId, tnodeId;
  int sdomainId, snodeId;
  int x,y;
} info[MAX_N];

// Store graph
int f[MAX_N][MAX_N], mid[MAX_N][MAX_N];
int path[MAX_N];
int len;

int n, m;

bool isLeaf[MAX_N], mk[MAX_N];

int USE_BFS;
int PATH_OPTION;
int GRAPH_TYPE;
int OUTPUT_MODE;

int initTS(const char* f_name) {
  char s[MAXL];

  ifstream fin;

  fin.open(f_name);
  if (!fin.is_open()) 
    return 1;

  fin.getline(s, MAXL);
  fin >> n >> m;

  m = m / 2;
  int node_id, t_d, t_n, s_d, s_n, x_c, y_c;
  char type;
  while (true) {
    fin.getline(s, MAXL);
    if (strstr(s, "VERTICES")) {
      for (int i = 0; i < n; ++i) {
	fin.getline(s, MAXL);
	sscanf(s, "%d %c", &node_id, &type);
	if (type == 'T') {
	  sscanf(s, "%d %c:%d.%d %d %d", &node_id, &type, &t_d, &t_n, &x_c, &y_c);
	  info[node_id].isT = 1;
	  info[node_id].tdomainId = t_d;
	  info[node_id].tnodeId = t_n;
	  info[node_id].x = x_c;
	  info[node_id].y = y_c;
	}
	else if (type == 'S') {
	  sscanf(s, "%d %c:%d.%d/%d.%d %d %d", &node_id, &type, &t_d, &t_n, &s_d, &s_n, &x_c, &y_c);
	  info[node_id].isT = 0;
	  info[node_id].tdomainId = t_d;
	  info[node_id].tnodeId = t_n;
	  info[node_id].sdomainId = s_d;
	  info[node_id].snodeId = s_n;
	  info[node_id].x = x_c;
	  info[node_id].y = y_c;
	}
      }
      break;
    }
  }

  if (OUTPUT_MODE & TOPO)
    cout <<  n << " " << m << endl;
  while (true) {
    fin.getline(s, MAXL);
    int x, y, a, b;
    if (strstr(s, "EDGES")) {
      for (int i = 0; i < n ; ++i)
	for (int j = 0; j < n; ++j)
	  if (i == j) {
	    f[i][j] = 0;
	  }
	  else {
	    f[i][j] = MAX_PATH_LENGTH;
	    mid[i][j] = -1;
	  }
      for (int i = 0; i < m ; ++i) {
	fin >> x >> y >> a >> b;
	if (OUTPUT_MODE & TOPO)
	  cout << x << " " << y << " " << a << endl;
	f[x][y] = f[y][x] = a;
        mid[x][y] = mid[y][x] = -1;
	//	mid[x][y] = y;
	//  mid[y][x] = x;
      }
      break;
    }
  }

  fin.close();
  return 0;
}

int initRAND(const char* f_name) {
  char s[MAXL];

  ifstream fin;

  fin.open(f_name);
  if (!fin.is_open()) 
    return 1;

  fin.getline(s, MAXL);
  fin >> n >> m;

  m = m / 2;
  int node_id, t_d, t_n, s_d, s_n, x_c, y_c;
  char type;
  while (true) {
    fin.getline(s, MAXL);
    if (strstr(s, "VERTICES")) {
      for (int i = 0; i < n; ++i) {
	fin.getline(s, MAXL);
	sscanf(s, "%d %d %d %d", &node_id, &t_d, &x_c, &y_c);
	info[node_id].isT = 0;
	info[node_id].tdomainId = t_d;
	info[node_id].tnodeId = t_d;
	info[node_id].sdomainId = t_d;
	info[node_id].snodeId = t_d;
	info[node_id].x = x_c;
	info[node_id].y = y_c;
      }
      break;
    }
  }

  if (OUTPUT_MODE & TOPO)
    cout << n << " " << m << endl;
  while (true) {
    fin.getline(s, MAXL);
    int x, y, a, b;
    if (strstr(s, "EDGES")) {
      for (int i = 0; i < n ; ++i)
	for (int j = 0; j < n; ++j)
	  if (i == j)
	    f[i][j] = 0;
	  else {
	    f[i][j] = MAX_PATH_LENGTH;
	    mid[i][j] = -1;
	  }
      for (int i = 0; i < m ; ++i) {
	fin >> x >> y >> a >> b;
	if (OUTPUT_MODE & TOPO)
	  cout << x << " " << y << " " << a << endl;
	f[x][y] = f[y][x] = a;
        mid[x][y] = mid[y][x] = -1;
	//	mid[x][y] = y;
	//  mid[y][x] = x;
      }
      break;
    }
  }

  fin.close();
  return 0;

}

// Read File
int init(const char* filename) {
  if (GRAPH_TYPE == TS)
    return initTS(filename);
  else if (GRAPH_TYPE == RAND)
    return initRAND(filename);
  return 1;
}

void dfs(int k) {
  mk[k] = 1;
  isLeaf[k] = 1;
  
  for (int i = 0; i < n ; ++i)
    if (f[k][i] > 0 && f[k][i] < MAX_PATH_LENGTH && !mk[i]) {
      isLeaf[k] = 0;
      dfs(i);
    }
}

void bfs(int root) {
  int q[MAX_N];
  int l = 0;
  int k;

  q[l++] = root;
  mk[root] = 1;
  for (int s = 0; s < l; ++s) {
    k = q[s];
    isLeaf[k] = 1;
    for (int i = 0; i < n; ++i)
      if (f[k][i] > 0 && f[k][i] < MAX_PATH_LENGTH && !mk[i]) {
	isLeaf[k] = 0;
	mk[i] = 1;
	q[l++] = i;
      }
  }
  int cnt = 0;
  for (int i = 0; i < n ; ++i)
    if (isLeaf[i])
      ++cnt;
}

void find_leaves() {
  if (GRAPH_TYPE == TS) {
    memset(isLeaf, 0, sizeof(isLeaf));
    memset(mk, 0, sizeof(mk));
    for (int i = 0; i < n; ++i)
      if (info[i].isT) {
	if (USE_BFS)
	  bfs(i);
	else
	  dfs(i);
	break;
      }
  }
  else if (GRAPH_TYPE == RAND && !USE_BFS) {
    for (int i = 0; i < n ; ++i)
      isLeaf[i] = 1;
  }
  else if (GRAPH_TYPE == RAND && USE_BFS) {
    memset(isLeaf, 0, sizeof(isLeaf));
    memset(mk, 0, sizeof(mk));
    bfs(0);
    isLeaf[0] = 1;
  }
}

void floyd() {
  for (int k = 0; k < n ; ++k)
    for (int i = 0; i < n; ++i)
      for (int j = 0; j < n; ++j)
	if (f[i][k] + f[k][j] < f[i][j]) {
	  f[i][j] = f[i][k] + f[k][j];
	  mid[i][j] = k;
	}
}

bool isStub(int k) {
  return !info[k].isT && isLeaf[k];
}

void output_leaves() {
  if (!(OUTPUT_MODE & TOPO))
    return;

  int cnt = 0;
  for (int i = 0 ; i < n; ++i)
    if (isStub(i))
      ++cnt;

  cout << cnt << endl;
  for (int i = 0; i < n; ++i)
    if (isStub(i))
      cout << i << endl;
}


void find_path(int x, int y) {
  if (mid[x][y] < 0) {
    path[len++] = y;
    return;
  }
  find_path(x, mid[x][y]);
  find_path(mid[x][y], y);
}


void output_paths_tree() {
  int n_path = 0;
  int n_stub = 0;

  for (int i = 0; i < n; ++i)
    if (isStub(i))
      ++n_stub;

  for (int i = 0; i < n; ++i) 
    if (isStub(i)) {
      for (int j = i + 1; j < n; ++j)
	if (isStub(j) && i != j)
	  if (info[i].sdomainId != info[j].sdomainId)
	    ++n_path;
      break;
    }
	
  //  cout << n << " " << n_path << " " << n_stub << endl;
  cout << n_path << endl;
  for (int i = 0; i < n; ++i)
    if (isStub(i)) {
      len = 0;
      path[len++] = i;
      for (int j = i + 1; j < n; ++j)
	if (isStub(j) && i != j)
	  if (info[i].sdomainId != info[j].sdomainId) {
            len = 1;
	    find_path(i, j);
	    cout << (len+1) << " " << i;
	    for (int k = 0; k < len; ++k)
	      cout << " " << path[k];
	    cout << endl;
	  }
      break;
    }
}


void output_paths_1way() {
  int n_path = 0;
  int n_stub = 0;

  for (int i = 0; i < n; ++i) 
    if (isStub(i)) {
      ++n_stub;
      for (int j = i + 1; j < n; ++j)
	if (isStub(j) && i != j)
	  if (info[i].sdomainId != info[j].sdomainId)
	    ++n_path;
    }
	
  cout << n_path << endl;
  //  cout << n << " " << n_path << " " << n_stub << endl;
  for (int i = 0; i < n; ++i)
    if (isStub(i))
    for (int j = i + 1; j < n; ++j)
      if (isStub(j) && i != j)
	if (info[i].sdomainId != info[j].sdomainId) {
	  len = 0;
	  find_path(i, j);
	  cout << (len+1) << " " << i;
	  for (int k = 0; k < len; ++k)
	    cout << " " << path[k];
	  cout << endl;
	}
}

void output_paths_2way() {
  int n_path = 0;
  int n_stub = 0;

  for (int i = 0; i < n; ++i) 
    if (isStub(i)) {
      ++n_stub;
      for (int j = 0; j < n; ++j)
	if (isStub(j))
	  //	  if (info[i].sdomainId != info[j].sdomainId)
	    ++n_path;
    }
	
  cout << n_path << endl;
  //  cout << n << " " << n_path << " " << n_stub << endl;
  for (int i = 0; i < n; ++i)
    if (isStub(i))
    for (int j = 0; j < n; ++j)
      if (isStub(j)) {
	//	if (info[i].sdomainId != info[j].sdomainId) {
	  len = 0;
	  find_path(i, j);
	  cout << (len+1) << " " << i;
	  for (int k = 0; k < len; ++k)
	    cout << " " << path[k];
	  cout << endl;
	}
}

void output_paths() {
  if (!(OUTPUT_MODE & ROUTES))
    return;

  if (PATH_OPTION == TREE)
    output_paths_tree();
  else if (PATH_OPTION == ONEWAY)
    output_paths_1way();
  else if (PATH_OPTION == TWOWAY)
    output_paths_2way();
}

int main(int argc, char* argv[]) {
  sscanf(argv[2], "%d", &GRAPH_TYPE);
  sscanf(argv[3], "%d", &USE_BFS);
  sscanf(argv[4], "%d", &PATH_OPTION);
  sscanf(argv[5], "%d", &OUTPUT_MODE);

  if (init(argv[1]))
    return 1;

  find_leaves();
  output_leaves();
  floyd();
  output_paths();
}
