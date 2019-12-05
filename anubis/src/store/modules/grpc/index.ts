import { Module } from 'vuex';
import { getters } from './getters';
import { actions } from './actions';
import { mutations } from './mutations';
import { GrpcState } from './types';
import { RootState } from '@/types/store';

import * as grpcWeb from 'grpc-web';
// import {} from 'generated/protobuf/ts/replayer_pb';
// import {ReplayerClient} from '../../../../generated/protobuf/ts/ReplayerServiceClientPb';

export const state: GrpcState = {
  // replayerClient: new ReplayerClient('http://localhost:8080', null, null)
};

const namespaced: boolean = true;

export const grpc: Module<GrpcState, RootState> = {
  namespaced,
  state,
  getters,
  actions,
  mutations
};