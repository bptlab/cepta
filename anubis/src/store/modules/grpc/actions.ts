import { ActionTree } from 'vuex';
import { GrpcState } from './types';
import { RootState } from '@/types/store';


export const actions: ActionTree<GrpcState, RootState> = {
  fetchData({ commit }): any {
    /*
    * GRPC_TEST(_) {
    var client = new GreeterClient('http://localhost:8080');

    var request = new HelloRequest();
    request.setName('World');

    client.sayHello(request, {}, (err, response) => {
      console.log(response.getMessage());
    });
  },*/
  }
};