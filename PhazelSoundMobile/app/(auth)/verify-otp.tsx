import React, { useState, useRef, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  TouchableOpacity,
  Alert,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { LinearGradient } from 'expo-linear-gradient';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Button } from '../../src/components/Button';
import { Colors } from '../../src/constants/colors';
import authService from '../../src/features/auth/auth.service';

export default function VerifyOtpScreen() {
  const router = useRouter();
  const { email } = useLocalSearchParams<{ email: string }>();
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [loading, setLoading] = useState(false);
  const [resendLoading, setResendLoading] = useState(false);
  const [timer, setTimer] = useState(300);
  const inputRefs = useRef<(TextInput | null)[]>([]);

  useEffect(() => {
    const interval = setInterval(() => {
      setTimer((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const handleOtpChange = (text: string, index: number) => {
    if (text.length > 1) {
      const digits = text.split('').slice(0, 6);
      const newOtp = [...otp];
      digits.forEach((digit, i) => {
        if (index + i < 6) {
          newOtp[index + i] = digit;
        }
      });
      setOtp(newOtp);
      const nextIndex = Math.min(index + digits.length, 5);
      inputRefs.current[nextIndex]?.focus();
      return;
    }

    const newOtp = [...otp];
    newOtp[index] = text;
    setOtp(newOtp);

    if (text && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyPress = (e: any, index: number) => {
    if (e.nativeEvent.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handleVerify = async () => {
    const otpCode = otp.join('');
    if (otpCode.length !== 6) {
      Alert.alert('Lỗi', 'Vui lòng nhập đủ 6 số');
      return;
    }

    setLoading(true);
    try {
      await authService.verifyOtp(email, otpCode);
      Alert.alert('Thành công', 'Tài khoản đã được kích hoạt!', [
        {
          text: 'Đăng nhập',
          onPress: () => router.replace('/(auth)/login'),
        },
      ]);
    } catch (error: any) {
      Alert.alert('Lỗi', error.message || 'Xác thực thất bại');
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    setResendLoading(true);
    try {
      await authService.resendRegisterOtp(email);
      Alert.alert('Thành công', 'Mã OTP mới đã được gửi đến email của bạn');
      setTimer(300);
      setOtp(['', '', '', '', '', '']);
      inputRefs.current[0]?.focus();
    } catch (error: any) {
      Alert.alert('Lỗi', error.message || 'Không thể gửi lại mã OTP');
    } finally {
      setResendLoading(false);
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
            <Ionicons name="mail-outline" size={64} color={Colors.accent.coral} />
          </View>

          <Text style={styles.title}>Xác thực tài khoản</Text>
          <Text style={styles.subtitle}>
            Mã OTP đã được gửi đến{'\n'}
            <Text style={styles.email}>{email}</Text>
          </Text>

          <View style={styles.otpContainer}>
            {otp.map((digit, index) => (
              <TextInput
                key={index}
                ref={(ref) => (inputRefs.current[index] = ref)}
                style={[
                  styles.otpInput,
                  digit && styles.otpInputFilled,
                ]}
                value={digit}
                onChangeText={(text) => handleOtpChange(text, index)}
                onKeyPress={(e) => handleKeyPress(e, index)}
                keyboardType="number-pad"
                maxLength={1}
                selectTextOnFocus
              />
            ))}
          </View>

          <View style={styles.timerContainer}>
            <Ionicons name="time-outline" size={16} color={Colors.text.muted} />
            <Text style={styles.timerText}>
              Mã có hiệu lực trong {formatTime(timer)}
            </Text>
          </View>

          <Button
            title="Xác thực"
            onPress={handleVerify}
            loading={loading}
            disabled={otp.join('').length !== 6}
            style={styles.verifyButton}
          />

          <View style={styles.resendContainer}>
            <Text style={styles.resendText}>Không nhận được mã? </Text>
            <TouchableOpacity
              onPress={handleResend}
              disabled={resendLoading || timer > 0}
            >
              <Text
                style={[
                  styles.resendLink,
                  (resendLoading || timer > 0) && styles.resendLinkDisabled,
                ]}
              >
                {resendLoading ? 'Đang gửi...' : 'Gửi lại'}
              </Text>
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
  email: {
    color: Colors.accent.coral,
    fontWeight: '600',
  },
  otpContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    gap: 12,
    marginBottom: 24,
  },
  otpInput: {
    width: 48,
    height: 56,
    borderRadius: 12,
    backgroundColor: Colors.background.input,
    borderWidth: 2,
    borderColor: 'transparent',
    fontSize: 24,
    fontWeight: '600',
    color: Colors.text.primary,
    textAlign: 'center',
  },
  otpInputFilled: {
    borderColor: Colors.accent.coral,
    backgroundColor: Colors.background.card,
  },
  timerContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginBottom: 32,
  },
  timerText: {
    fontSize: 14,
    color: Colors.text.muted,
  },
  verifyButton: {
    width: '100%',
    marginBottom: 24,
  },
  resendContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  resendText: {
    fontSize: 14,
    color: Colors.text.secondary,
  },
  resendLink: {
    fontSize: 14,
    color: Colors.accent.coral,
    fontWeight: '600',
  },
  resendLinkDisabled: {
    opacity: 0.5,
  },
});
