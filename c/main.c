#include "libsentiments.h"

#include <stdio.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>

char* read_file(const char * file) {
  FILE *fp;
  long lSize;
  char *buffer;

  fp = fopen ( file , "rb" );

  fseek( fp , 0L , SEEK_END);
  lSize = ftell( fp );
  rewind( fp );

  /* allocate memory for entire content */
  buffer = calloc( 1, 8 * lSize+1 );
  if( !buffer ) fclose(fp),fputs("memory alloc fails",stderr),exit(1);

  /* copy the file into the buffer */
  if( 1!=fread( buffer , lSize, 1 , fp) )
    fclose(fp),free(buffer),fputs("entire read fails",stderr),exit(1);

  fclose(fp);
  return buffer;
}

int main(int argc, char** argv) {
  graal_create_isolate_params_t isolate_params = {};
  graal_isolate_t* isolate;
  if (graal_create_isolate(&isolate_params, &isolate)) {
    return -1;
  }
  char* tweets = read_file("data/ether-tweets");
  char* prices = read_file("data/eth-price.csv");
  printf("Correlation %f\n", correlate_tweets_with_market(isolate, prices, tweets));
  return 0;
}
