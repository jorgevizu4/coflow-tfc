import React, { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, StyleSheet,
  ActivityIndicator, KeyboardAvoidingView, Platform, Alert, ScrollView,
} from 'react-native';
import { authService } from '../services/authService';
import { useAuth } from '../auth/AuthContext';
import { setApiToken } from '../services/apiClient';
import { COLORS } from '../components/theme';

export default function SignupScreen({ navigation }: any) {
  const { login } = useAuth();
  const [empresaNombre, setEmpresaNombre] = useState('');
  const [nombre, setNombre] = useState('');
  const [apellidos, setApellidos] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordRepeat, setPasswordRepeat] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSignup = async () => {
    if (!empresaNombre || !nombre || !apellidos || !email || !password) {
      Alert.alert('Error', 'Rellena todos los campos');
      return;
    }
    if (password !== passwordRepeat) {
      Alert.alert('Error', 'Las contraseñas no coinciden');
      return;
    }
    setLoading(true);
    try {
      const userData = await authService.signup(empresaNombre, nombre, apellidos, email, password);
      setApiToken(userData.token);
      await login(userData);
    } catch (e: any) {
      Alert.alert('Error en el registro', e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
        <View style={styles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
            <Text style={styles.backText}>← Volver</Text>
          </TouchableOpacity>
          <Text style={styles.title}>Crear empresa y cuenta</Text>
          <Text style={styles.subtitle}>Regístrate como administrador de una nueva empresa</Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.sectionLabel}>Datos de la empresa</Text>

          <Text style={styles.label}>Nombre de la empresa</Text>
          <TextInput style={styles.input} placeholder="Mi Empresa S.L." placeholderTextColor={COLORS.textMuted}
            value={empresaNombre} onChangeText={setEmpresaNombre} />

          <Text style={[styles.sectionLabel, { marginTop: 20 }]}>Tu cuenta</Text>

          <Text style={styles.label}>Nombre</Text>
          <TextInput style={styles.input} placeholder="Jorge" placeholderTextColor={COLORS.textMuted}
            value={nombre} onChangeText={setNombre} />

          <Text style={styles.label}>Apellidos</Text>
          <TextInput style={styles.input} placeholder="García López" placeholderTextColor={COLORS.textMuted}
            value={apellidos} onChangeText={setApellidos} />

          <Text style={styles.label}>Email</Text>
          <TextInput style={styles.input} placeholder="jorge@empresa.com" placeholderTextColor={COLORS.textMuted}
            value={email} onChangeText={setEmail} autoCapitalize="none" keyboardType="email-address" />

          <Text style={styles.label}>Contraseña</Text>
          <TextInput style={styles.input} placeholder="••••••••" placeholderTextColor={COLORS.textMuted}
            value={password} onChangeText={setPassword} secureTextEntry />

          <Text style={styles.label}>Repetir contraseña</Text>
          <TextInput style={styles.input} placeholder="••••••••" placeholderTextColor={COLORS.textMuted}
            value={passwordRepeat} onChangeText={setPasswordRepeat} secureTextEntry />

          <TouchableOpacity style={[styles.btn, loading && styles.btnDisabled]} onPress={handleSignup} disabled={loading}>
            {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.btnText}>Crear cuenta</Text>}
          </TouchableOpacity>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.deepBlue },
  scroll: { flexGrow: 1, padding: 24 },
  header: { marginBottom: 24, marginTop: 8 },
  backBtn: { marginBottom: 16 },
  backText: { color: COLORS.primary, fontSize: 15 },
  title: { color: '#fff', fontSize: 24, fontWeight: 'bold' },
  subtitle: { color: COLORS.textMuted, fontSize: 14, marginTop: 4 },
  card: { backgroundColor: COLORS.surface, borderRadius: 16, padding: 24 },
  sectionLabel: { color: COLORS.primary, fontSize: 13, fontWeight: '700', textTransform: 'uppercase', letterSpacing: 1, marginBottom: 4 },
  label: { color: COLORS.textMuted, fontSize: 13, marginBottom: 6, marginTop: 12 },
  input: {
    backgroundColor: COLORS.surfaceLight, color: '#fff', borderRadius: 10,
    paddingHorizontal: 14, paddingVertical: 12, fontSize: 15,
    borderWidth: 1, borderColor: COLORS.border,
  },
  btn: { backgroundColor: COLORS.primary, borderRadius: 10, paddingVertical: 14, alignItems: 'center', marginTop: 24 },
  btnDisabled: { opacity: 0.6 },
  btnText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
});
