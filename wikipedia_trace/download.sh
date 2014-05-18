for OUTPUT in $(cat filename.txt)
do
    wget "http://www.wikibench.eu/wiki/2007-09/"$OUTPUT
done
