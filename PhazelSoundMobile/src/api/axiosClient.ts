import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const axiosClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

axiosClient.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

axiosClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      const message = error.response.data?.message || error.response.data?.error || 'Đã có lỗi xảy ra';
      return Promise.reject(new Error(message));
    } else if (error.request) {
      return Promise.reject(new Error('Không thể kết nối đến server'));
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
