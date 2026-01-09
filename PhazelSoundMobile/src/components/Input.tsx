import React, { useState } from 'react';
import {
  TextInput,
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  TextInputProps,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../constants/colors';

interface InputProps extends TextInputProps {
  label?: string;
  error?: string;
  leftIcon?: keyof typeof Ionicons.glyphMap;
  rightIcon?: keyof typeof Ionicons.glyphMap;
  onRightIconPress?: () => void;
}

export const Input: React.FC<InputProps> = ({
  label,
  error,
  leftIcon,
  rightIcon,
  onRightIconPress,
  secureTextEntry,
  ...props
}) => {
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [isFocused, setIsFocused] = useState(false);

  const togglePasswordVisibility = () => {
    setIsPasswordVisible(!isPasswordVisible);
  };

  const showPasswordToggle = secureTextEntry;
  const finalRightIcon = showPasswordToggle
    ? isPasswordVisible
      ? 'eye-off-outline'
      : 'eye-outline'
    : rightIcon;

  const finalOnRightIconPress = showPasswordToggle
    ? togglePasswordVisibility
    : onRightIconPress;

  return (
    <View style={styles.container}>
      {label && <Text style={styles.label}>{label}</Text>}
      <View
        style={[
          styles.inputContainer,
          isFocused && styles.inputFocused,
          error && styles.inputError,
        ]}
      >
        {leftIcon && (
          <Ionicons
            name={leftIcon}
            size={20}
            color={Colors.text.muted}
            style={styles.leftIcon}
          />
        )}
        <TextInput
          style={[styles.input, leftIcon && styles.inputWithLeftIcon]}
          placeholderTextColor={Colors.text.muted}
          secureTextEntry={secureTextEntry && !isPasswordVisible}
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          {...props}
        />
        {finalRightIcon && (
          <TouchableOpacity
            onPress={finalOnRightIconPress}
            style={styles.rightIconContainer}
          >
            <Ionicons
              name={finalRightIcon}
              size={20}
              color={Colors.text.muted}
            />
          </TouchableOpacity>
        )}
      </View>
      {error && <Text style={styles.errorText}>{error}</Text>}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    width: '100%',
    marginBottom: 16,
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: Colors.text.secondary,
    marginBottom: 8,
  },
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: Colors.background.input,
    borderRadius: 12,
    borderWidth: 2,
    borderColor: 'transparent',
    paddingHorizontal: 16,
    height: 56,
  },
  inputFocused: {
    borderColor: Colors.accent.coral,
    backgroundColor: Colors.background.card,
  },
  inputError: {
    borderColor: Colors.status.error,
  },
  input: {
    flex: 1,
    fontSize: 16,
    color: Colors.text.primary,
    height: '100%',
  },
  inputWithLeftIcon: {
    marginLeft: 8,
  },
  leftIcon: {
    marginRight: 0,
  },
  rightIconContainer: {
    padding: 4,
    marginLeft: 8,
  },
  errorText: {
    fontSize: 12,
    color: Colors.status.error,
    marginTop: 4,
    marginLeft: 4,
  },
});
