import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Alert,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { LinearGradient } from 'expo-linear-gradient';
import { useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Input } from '../../src/components/Input';
import { Button } from '../../src/components/Button';
import { Colors } from '../../src/constants/colors';
import authService from '../../src/features/auth/auth.service';

export default function ForgotPasswordScreen() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const validate = () => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email.trim()) {
      setError('Email không được để trống');
      return false;
    }
    if (!emailRegex.test(email)) {
      setError('Email không hợp lệ');
      return false;
    }
    setError('');
    return true;
  };

  const handleSendOtp = async () => {
    if (!validate()) return;

    setLoading(true);
    try {
      await authService.forgotPassword(email.trim());
      Alert.alert('Thành công', 'Mã OTP đã được gửi đến email của bạn', [
        {
          text: 'OK',
          onPress: () => router.push({
            pathname: '/(auth)/reset-password',
            params: { email: email.trim() },
          }),
        },
      ]);
    } catch (error: any) {
      Alert.alert('Lỗi', error.message || 'Không thể gửi mã OTP');
    } finally {
      setLoading(false);
    }
  };

  return (
    <LinearGradient colors={Colors.gradient.primary} style={styles.container}>
      <SafeAreaView style={styles.safeArea}>
        <TouchableOpacity
          onPress={() => router.back()}
          style={styles.backButton}
        >
          <Ionicons name="arrow-back" size={24} color={Colors.text.primary} />
        </TouchableOpacity>

        <View style={styles.content}>
          <View style={styles.iconContainer}>
            <Ionicons name="lock-closed-outline" size={64} color={Colors.accent.coral} />
          </View>

          <Text style={styles.title}>Quên mật khẩu?</Text>
          <Text style={styles.subtitle}>
            Nhập email của bạn để nhận mã xác thực{'\n'}đặt lại mật khẩu
          </Text>

          <View style={styles.form}>
            <Input
              label="Email"
              placeholder="Nhập email của bạn"
              value={email}
              onChangeText={(text) => {
                setEmail(text);
                setError('');
              }}
              error={error}
              leftIcon="mail-outline"
              autoCapitalize="none"
              keyboardType="email-address"
            />

            <Button
              title="Gửi mã xác thực"
              onPress={handleSendOtp}
              loading={loading}
              style={styles.sendButton}
            />

            <TouchableOpacity
              onPress={() => router.back()}
              style={styles.backToLogin}
            >
              <Ionicons name="arrow-back" size={16} color={Colors.accent.coral} />
              <Text style={styles.backToLoginText}>Quay lại đăng nhập</Text>
            </TouchableOpacity>
          </View>
        </View>
      </SafeAreaView>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  safeArea: {
    flex: 1,
    paddingHorizontal: 24,
  },
  backButton: {
    width: 40,
    height: 40,
    borderRadius: 12,
    backgroundColor: Colors.background.card,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 20,
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  iconContainer: {
    width: 120,
    height: 120,
    borderRadius: 60,
    backgroundColor: Colors.background.card,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 32,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: Colors.text.primary,
    marginBottom: 12,
  },
  subtitle: {
    fontSize: 16,
    color: Colors.text.secondary,
    textAlign: 'center',
    marginBottom: 40,
  },
  form: {
    width: '100%',
  },
  sendButton: {
    marginTop: 8,
    marginBottom: 24,
  },
  backToLogin: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
  },
  backToLoginText: {
    fontSize: 14,
    color: Colors.accent.coral,
    fontWeight: '600',
  },
});
