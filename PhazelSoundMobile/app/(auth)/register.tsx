import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
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

export default function RegisterScreen() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phoneNumber: '',
    password: '',
    confirmPassword: '',
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({
    name: '',
    email: '',
    phoneNumber: '',
    password: '',
    confirmPassword: '',
  });

  const validate = () => {
    let valid = true;
    const newErrors = {
      name: '',
      email: '',
      phoneNumber: '',
      password: '',
      confirmPassword: '',
    };

    if (!formData.name.trim()) {
      newErrors.name = 'Tên không được để trống';
      valid = false;
    } else if (formData.name.trim().length < 2) {
      newErrors.name = 'Tên phải có ít nhất 2 ký tự';
      valid = false;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!formData.email.trim()) {
      newErrors.email = 'Email không được để trống';
      valid = false;
    } else if (!emailRegex.test(formData.email)) {
      newErrors.email = 'Email không hợp lệ';
      valid = false;
    }

    if (!formData.password) {
      newErrors.password = 'Mật khẩu không được để trống';
      valid = false;
    } else if (formData.password.length < 6) {
      newErrors.password = 'Mật khẩu phải có ít nhất 6 ký tự';
      valid = false;
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'Vui lòng xác nhận mật khẩu';
      valid = false;
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Mật khẩu không khớp';
      valid = false;
    }

    setErrors(newErrors);
    return valid;
  };

  const handleRegister = async () => {
    if (!validate()) return;

    setLoading(true);
    try {
      await authService.register({
        name: formData.name.trim(),
        email: formData.email.trim(),
        phoneNumber: formData.phoneNumber.trim() || undefined,
        password: formData.password,
      });

      Alert.alert('Thành công', 'Vui lòng kiểm tra email để xác thực tài khoản', [
        {
          text: 'OK',
          onPress: () => router.push({
            pathname: '/(auth)/verify-otp',
            params: { email: formData.email.trim() },
          }),
        },
      ]);
    } catch (error: any) {
      Alert.alert('Lỗi', error.message || 'Đăng ký thất bại');
    } finally {
      setLoading(false);
    }
  };

  const updateField = (field: keyof typeof formData, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: '' }));
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
              <Text style={styles.title}>Tạo tài khoản</Text>
              <Text style={styles.subtitle}>
                Điền thông tin để bắt đầu hành trình âm nhạc
              </Text>
            </View>

            <View style={styles.form}>
              <Input
                label="Họ và tên"
                placeholder="Nhập họ và tên"
                value={formData.name}
                onChangeText={(text) => updateField('name', text)}
                error={errors.name}
                leftIcon="person-outline"
                autoCapitalize="words"
              />

              <Input
                label="Email"
                placeholder="Nhập email"
                value={formData.email}
                onChangeText={(text) => updateField('email', text)}
                error={errors.email}
                leftIcon="mail-outline"
                autoCapitalize="none"
                keyboardType="email-address"
              />

              <Input
                label="Số điện thoại (Không bắt buộc)"
                placeholder="Nhập số điện thoại"
                value={formData.phoneNumber}
                onChangeText={(text) => updateField('phoneNumber', text)}
                error={errors.phoneNumber}
                leftIcon="call-outline"
                keyboardType="phone-pad"
              />

              <Input
                label="Mật khẩu"
                placeholder="Nhập mật khẩu"
                value={formData.password}
                onChangeText={(text) => updateField('password', text)}
                error={errors.password}
                leftIcon="lock-closed-outline"
                secureTextEntry
              />

              <Input
                label="Xác nhận mật khẩu"
                placeholder="Nhập lại mật khẩu"
                value={formData.confirmPassword}
                onChangeText={(text) => updateField('confirmPassword', text)}
                error={errors.confirmPassword}
                leftIcon="lock-closed-outline"
                secureTextEntry
              />

              <Button
                title="Đăng ký"
                onPress={handleRegister}
                loading={loading}
                style={styles.registerButton}
              />

              <View style={styles.footer}>
                <Text style={styles.footerText}>Đã có tài khoản? </Text>
                <TouchableOpacity onPress={() => router.back()}>
                  <Text style={styles.footerLink}>Đăng nhập</Text>
                </TouchableOpacity>
              </View>
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
    marginBottom: 32,
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: Colors.text.primary,
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: Colors.text.secondary,
  },
  form: {
    width: '100%',
  },
  registerButton: {
    marginTop: 8,
    marginBottom: 24,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
  },
  footerText: {
    fontSize: 14,
    color: Colors.text.secondary,
  },
  footerLink: {
    fontSize: 14,
    color: Colors.accent.coral,
    fontWeight: '600',
  },
});
