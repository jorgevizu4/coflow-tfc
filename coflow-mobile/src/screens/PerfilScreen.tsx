import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Alert } from 'react-native';
import { useAuth } from '../auth/AuthContext';
import { setApiToken } from '../services/apiClient';
import { COLORS } from '../components/theme';

const ROL_LABEL: Record<string, string> = {
  ADMIN: 'Administrador',
  LIDER: 'Líder de equipo',
  REVISOR: 'Revisor',
  USER: 'Usuario',
};

const ROL_COLOR: Record<string, string> = {
  ADMIN: COLORS.danger,
  LIDER: COLORS.primary,
  REVISOR: COLORS.warning,
  USER: COLORS.success,
};

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.infoRow}>
      <Text style={styles.infoLabel}>{label}</Text>
      <Text style={styles.infoValue}>{value}</Text>
    </View>
  );
}

export default function PerfilScreen() {
  const { user, logout } = useAuth();

  const handleLogout = () => {
    Alert.alert('Cerrar sesión', '¿Seguro que quieres salir?', [
      { text: 'Cancelar', style: 'cancel' },
      {
        text: 'Salir', style: 'destructive', onPress: async () => {
          setApiToken(null);
          await logout();
        }
      },
    ]);
  };

  if (!user) return null;

  const iniciales = user.nombreCompleto
    .split(' ')
    .slice(0, 2)
    .map(n => n[0])
    .join('')
    .toUpperCase();

  return (
    <View style={styles.container}>
      {/* Avatar */}
      <View style={styles.header}>
        <View style={styles.avatarCircle}>
          <Text style={styles.avatarText}>{iniciales}</Text>
        </View>
        <Text style={styles.nombre}>{user.nombreCompleto}</Text>
        <View style={[styles.rolBadge, { backgroundColor: (ROL_COLOR[user.rol] ?? COLORS.primary) + '33', borderColor: ROL_COLOR[user.rol] ?? COLORS.primary }]}>
          <Text style={[styles.rolText, { color: ROL_COLOR[user.rol] ?? COLORS.primary }]}>
            {ROL_LABEL[user.rol] ?? user.rol}
          </Text>
        </View>
      </View>

      {/* Info */}
      <View style={styles.card}>
        <Text style={styles.sectionTitle}>Información de cuenta</Text>
        <InfoRow label="Email" value={user.email} />
        <InfoRow label="Empresa" value={user.empresaNombre} />
        <InfoRow label="ID de usuario" value={String(user.usuarioId)} />
        <InfoRow label="ID de empresa" value={String(user.empresaId)} />
      </View>

      {/* Nota conexión a la nube */}
      <View style={styles.cloudCard}>
        <Text style={styles.cloudTitle}>☁️ Conexión a la nube</Text>
        <Text style={styles.cloudText}>
          Esta app conecta con el backend de CoFlow desplegado en la nube.
          Los datos se sincronizan en tiempo real con la base de datos PostgreSQL
          alojada en el servidor. La autenticación usa JWT con expiración automática.
        </Text>
        <Text style={styles.cloudNote}>
          La URL del servidor se configura en{' '}
          <Text style={styles.cloudCode}>src/auth/AuthContext.tsx</Text>
        </Text>
      </View>

      {/* Cerrar sesión */}
      <TouchableOpacity style={styles.logoutBtn} onPress={handleLogout}>
        <Text style={styles.logoutText}>Cerrar sesión</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.deepBlue, padding: 20 },
  header: { alignItems: 'center', marginBottom: 24, marginTop: 8 },
  avatarCircle: { width: 80, height: 80, borderRadius: 40, backgroundColor: COLORS.primary, justifyContent: 'center', alignItems: 'center', marginBottom: 12 },
  avatarText: { color: '#fff', fontSize: 30, fontWeight: 'bold' },
  nombre: { color: '#fff', fontSize: 22, fontWeight: 'bold', marginBottom: 8 },
  rolBadge: { paddingHorizontal: 14, paddingVertical: 5, borderRadius: 20, borderWidth: 1 },
  rolText: { fontSize: 13, fontWeight: '600' },
  card: { backgroundColor: COLORS.surface, borderRadius: 14, padding: 16, marginBottom: 16, borderWidth: 1, borderColor: COLORS.border },
  sectionTitle: { color: '#fff', fontSize: 15, fontWeight: '700', marginBottom: 12 },
  infoRow: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: COLORS.border },
  infoLabel: { color: COLORS.textMuted, fontSize: 14 },
  infoValue: { color: '#fff', fontSize: 14, fontWeight: '500', maxWidth: '60%', textAlign: 'right' },
  cloudCard: { backgroundColor: COLORS.surface, borderRadius: 14, padding: 16, marginBottom: 24, borderWidth: 1, borderColor: COLORS.primary + '55' },
  cloudTitle: { color: COLORS.primary, fontSize: 14, fontWeight: '700', marginBottom: 8 },
  cloudText: { color: COLORS.textMuted, fontSize: 13, lineHeight: 20, marginBottom: 8 },
  cloudNote: { color: COLORS.textMuted, fontSize: 12 },
  cloudCode: { color: COLORS.primary, fontFamily: Platform.OS === 'ios' ? 'Courier' : 'monospace' },
  logoutBtn: { backgroundColor: COLORS.danger + '22', borderWidth: 1, borderColor: COLORS.danger, borderRadius: 12, paddingVertical: 14, alignItems: 'center' },
  logoutText: { color: COLORS.danger, fontWeight: '700', fontSize: 15 },
});

const Platform = { OS: 'ios' };
