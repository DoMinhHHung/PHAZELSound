import axiosClient from '../../api/axiosClient';
import {
  RegisterRequest,
  LoginRequest,
  AuthResponse,
  ResetPasswordRequest,
} from './auth.types';

class AuthService {
  async register(data: RegisterRequest): Promise<string> {
    const response = await axiosClient.post('/auth/register', data);
    return response.data;
  }

  async verifyOtp(email: string, otp: string): Promise<string> {
    const response = await axiosClient.post('/auth/verify', null, {
      params: { email, otp },
    });
    return response.data;
  }

  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await axiosClient.post<AuthResponse>('/auth/login', data);
    return response.data;
  }

  async resendRegisterOtp(email: string): Promise<string> {
    const response = await axiosClient.post('/auth/resend-register-otp', null, {
      params: { email },
    });
    return response.data;
  }

  async forgotPassword(email: string): Promise<string> {
    const response = await axiosClient.post('/auth/forgot-password', null, {
      params: { email },
    });
    return response.data;
  }

  async resetPassword(data: ResetPasswordRequest): Promise<string> {
    const response = await axiosClient.post('/auth/reset-password', data);
    return response.data;
  }
}

export default new AuthService();
