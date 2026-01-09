import React, { useState, useRef, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  TouchableOpacity,
  Alert,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { LinearGradient } from 'expo-linear-gradient';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Input } from '../../src/components/Input';
import { Button } from '../../src/components/Button';
import { Colors } from '../../src/constants/colors';
import authService from '../../src/features/auth/auth.service';

export default function ResetPasswordScreen() {
  const router = useRouter();
  const { email } = useLocalSearchParams<{ email: string }>();
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({
    newPassword: '',
    confirmPassword: '',
  });
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

  const validate = () => {
    let valid = true;
    const newErrors = { newPassword: '', confirmPassword: '' };

    if (!newPassword) {
      newErrors.newPassword = 'Mật khẩu mới không được để trống';
      valid = false;
    } else if (newPassword.length < 6) {
      newErrors.newPassword = 'Mật khẩu phải có ít nhất 6 ký tự';
      valid = false;
    }

    if (!confirmPassword) {
      newErrors.confirmPassword = 'Vui lòng xác nhận mật khẩu';
      valid = false;
    } else if (newPassword !== confirmPassword) {
      newErrors.confirmPassword = 'Mật khẩu không khớp';
      valid = false;
    }

    setErrors(newErrors);
    return valid;
  };

  const handleResetPassword = async () => {
    const otpCode = otp.join('');
    if (otpCode.length !== 6) {
      Alert.alert('Lỗi', 'Vui lòng nhập đủ 6 số mã OTP');
      return;
    }

    if (!validate()) return;

    setLoading(true);
    try {
      await authService.resetPassword({
        email,
        otp: otpCode,
        newPassword,
      });

      Alert.alert('Thành công', 'Mật khẩu đã được đặt lại!', [
        {
          text: 'Đăng nhập',
          onPress: () => router.replace('/(auth)/login'),
        },
      ]);
    } catch (error: any) {
      Alert.alert('Lỗi', error.message || 'Đặt lại mật khẩu thất bại');
    } finally {
      setLoading(false);
    }
  };

  return (
    <LinearGradient colors={Colors.gradient.primary} style={styles.container}>
      <SafeAreaView style={styles.safeArea}>
        <KeyboardAvoidingView
          behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
          style={styles.keyboardView}
        >
          <ScrollView
            contentContainerStyle={styles.scrollContent}
            showsVerticalScrollIndicator={false}
          >
            <TouchableOpacity
              onPress={() => router.back()}
              style={styles.backButton}
            >
              <Ionicons name="arrow-back" size={24} color={Colors.text.primary} />
            </TouchableOpacity>

            <View style={styles.header}>
              <View style={styles.iconContainer}>
                <Ionicons name="key-outline" size={64} color={Colors.accent.coral} />
              </View>

              <Text style={styles.title}>Đặt lại mật khẩu</Text>
              <Text style={styles.subtitle}>
                Nhập mã OTP và mật khẩu mới của bạn
              </Text>
            </View>

            <View style={styles.form}>
              <Text style={styles.sectionLabel}>Mã OTP</Text>
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

              <Input
                label="Mật khẩu mới"
                placeholder="Nhập mật khẩu mới"
                value={newPassword}
                onChangeText={(text) => {
                  setNewPassword(text);
                  setErrors((prev) => ({ ...prev, newPassword: '' }));
                }}
                error={errors.newPassword}
                leftIcon="lock-closed-outline"
                secureTextEntry
              />

              <Input
                label="Xác nhận mật khẩu"
                placeholder="Nhập lại mật khẩu mới"
                value={confirmPassword}
                onChangeText={(text) => {
                  setConfirmPassword(text);
                  setErrors((prev) => ({ ...prev, confirmPassword: '' }));
                }}
                error={errors.confirmPassword}
                leftIcon="lock-closed-outline"
                secureTextEntry
              />

              <Button
                title="Đặt lại mật khẩu"
                onPress={handleResetPassword}
                loading={loading}
                disabled={otp.join('').length !== 6}
                style={styles.resetButton}
              />
            </View>
          </ScrollView>
        </KeyboardAvoidingView>
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
  },
  keyboardView: {
    flex: 1,
  },
  scrollContent: {
    flexGrow: 1,
    paddingHorizontal: 24,
    paddingTop: 20,
    paddingBottom: 40,
  },
  backButton: {
    width: 40,
    height: 40,
    borderRadius: 12,
    backgroundColor: Colors.background.card,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 20,
  },
  header: {
    alignItems: 'center',
    marginBottom: 32,
  },
  iconContainer: {
    width: 100,
    height: 100,
    borderRadius: 50,
    backgroundColor: Colors.background.card,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 24,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: Colors.text.primary,
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: Colors.text.secondary,
    textAlign: 'center',
  },
  form: {
    width: '100%',
  },
  sectionLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: Colors.text.secondary,
    marginBottom: 12,
  },
  otpContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    gap: 12,
    marginBottom: 12,
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
    justifyContent: 'center',
    gap: 8,
    marginBottom: 32,
  },
  timerText: {
    fontSize: 14,
    color: Colors.text.muted,
  },
  resetButton: {
    marginTop: 8,
  },
});
