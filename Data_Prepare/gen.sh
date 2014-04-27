exe=./make_gt_itm_graph
folder=ts100/ts-100

g++ "$exe.cpp" -o "$exe"
for i in 0 1 2 3 4 5 6 7 8 9 
do
  echo "$exe ts100/ts100-$i.alt 0 1 2 1 > ts100/ts100-$i-topo.txt"
  $exe "ts100/ts100-$i.alt" 0 1 2 1 > "ts100/ts100-$i-topo.txt"
  echo "$exe ts100/ts100-$i.alt 0 1 2 2> ts100/ts100-$i-routing.txt"
  $exe "ts100/ts100-$i.alt" 0 1 2 2 > "ts100/ts100-$i-routing.txt"

done