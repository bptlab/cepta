export interface RootState {
  appName: string;
  appAllowsRegister: boolean;
  appAuthenticationAPI: string;
  appSignUpAPI: string;
  isCollapsed: boolean;
  authToken: string;
  authStatus: string;
}