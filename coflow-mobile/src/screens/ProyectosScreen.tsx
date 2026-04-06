import React, { useCallback, useEffect, useState } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  ActivityIndicator, RefreshControl, Modal, TextInput, Alert,
} from 'react-native';
import { proyectoService } from '../services/proyectoService';
import { Proyecto } from '../types/types';
import { COLORS, formatDate } from '../components/theme';
import { useAuth } from '../auth/AuthContext';

export default function ProyectosScreen({ navigation }: any) {
  const { user } = useAuth();
  const [proyectos, setProyectos] = useState<Proyecto[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [creando, setCreando] = useState(false);

  const cargar = useCallback(async () => {
    try {
      const res = await proyectoService.listar();
      setProyectos(res.data);
    } catch (e: any) {
      Alert.alert('Error', e.message);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { cargar(); }, [cargar]);

  const crearProyecto = async () => {
    if (!titulo.trim()) { Alert.alert('Error', 'El título es obligatorio'); return; }
    setCreando(true);
    try {
      await proyectoService.crear({ titulo: titulo.trim(), descripcion: descripcion.trim() || undefined });
      setShowModal(false);
      setTitulo('');
      setDescripcion('');
      await cargar();
    } catch (e: any) {
      Alert.alert('Error', e.message);
    } finally {
      setCreando(false);
    }
  };

  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color={COLORS.primary} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={proyectos}
        keyExtractor={p => String(p.id)}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); cargar(); }} tintColor={COLORS.primary} />}
        contentContainerStyle={{ padding: 16, paddingBottom: 100 }}
        ListEmptyComponent={<Text style={styles.empty}>No hay proyectos creados aún</Text>}
        renderItem={({ item }) => (
          <TouchableOpacity
            style={styles.card}
            onPress={() => navigation.navigate('Tareas', { proyectoId: item.id })}
            activeOpacity={0.8}
          >
            <View style={styles.cardTop}>
              <View style={styles.iconCircle}>
                <Text style={styles.iconText}>{item.titulo.charAt(0).toUpperCase()}</Text>
              </View>
              <View style={styles.cardInfo}>
                <Text style={styles.cardTitle}>{item.titulo}</Text>
                <Text style={styles.cardEmpresa}>{item.empresaNombre}</Text>
              </View>
            </View>

            {item.descripcion ? (
              <Text style={styles.descripcion} numberOfLines={2}>{item.descripcion}</Text>
            ) : null}

            <View style={styles.cardFooter}>
              {item.liderNombre && (
                <Text style={styles.meta}>👤 {item.liderNombre}</Text>
              )}
              <Text style={styles.meta}>📅 Inicio: {formatDate(item.fechaInicio)}</Text>
              {item.fechaFinEstimada && (
                <Text style={styles.meta}>🏁 Fin: {formatDate(item.fechaFinEstimada)}</Text>
              )}
            </View>
          </TouchableOpacity>
        )}
      />

      {(user?.rol === 'ADMIN' || user?.rol === 'LIDER') && (
        <TouchableOpacity style={styles.fab} onPress={() => setShowModal(true)}>
          <Text style={styles.fabText}>+</Text>
        </TouchableOpacity>
      )}

      <Modal visible={showModal} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={styles.modalCard}>
            <Text style={styles.modalTitle}>Nuevo proyecto</Text>

            <Text style={styles.label}>Título *</Text>
            <TextInput style={styles.input} placeholder="Nombre del proyecto"
              placeholderTextColor={COLORS.textMuted} value={titulo} onChangeText={setTitulo} />

            <Text style={styles.label}>Descripción</Text>
            <TextInput style={[styles.input, { height: 80 }]} placeholder="Descripción opcional…"
              placeholderTextColor={COLORS.textMuted} value={descripcion}
              onChangeText={setDescripcion} multiline />

            <View style={styles.modalActions}>
              <TouchableOpacity style={styles.cancelBtn} onPress={() => setShowModal(false)}>
                <Text style={{ color: COLORS.textMuted }}>Cancelar</Text>
              </TouchableOpacity>
              <TouchableOpacity style={[styles.btn, creando && styles.btnDisabled]} onPress={crearProyecto} disabled={creando}>
                {creando ? <ActivityIndicator color="#fff" size="small" /> : <Text style={styles.btnText}>Crear</Text>}
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.deepBlue },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: COLORS.deepBlue },
  card: { backgroundColor: COLORS.surface, borderRadius: 14, padding: 16, marginBottom: 12, borderWidth: 1, borderColor: COLORS.border },
  cardTop: { flexDirection: 'row', alignItems: 'center', marginBottom: 10 },
  iconCircle: { width: 44, height: 44, borderRadius: 22, backgroundColor: COLORS.primary, justifyContent: 'center', alignItems: 'center', marginRight: 12 },
  iconText: { color: '#fff', fontSize: 20, fontWeight: 'bold' },
  cardInfo: { flex: 1 },
  cardTitle: { color: '#fff', fontSize: 16, fontWeight: '700' },
  cardEmpresa: { color: COLORS.textMuted, fontSize: 12, marginTop: 2 },
  descripcion: { color: COLORS.textMuted, fontSize: 13, marginBottom: 10, lineHeight: 18 },
  cardFooter: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  meta: { color: COLORS.textMuted, fontSize: 12 },
  empty: { color: COLORS.textMuted, textAlign: 'center', marginTop: 40, fontSize: 15 },
  fab: { position: 'absolute', bottom: 24, right: 24, width: 56, height: 56, borderRadius: 28, backgroundColor: COLORS.primary, justifyContent: 'center', alignItems: 'center', elevation: 6 },
  fabText: { color: '#fff', fontSize: 28, fontWeight: 'bold', lineHeight: 32 },
  modalOverlay: { flex: 1, backgroundColor: '#000a', justifyContent: 'flex-end' },
  modalCard: { backgroundColor: COLORS.surface, borderTopLeftRadius: 20, borderTopRightRadius: 20, padding: 24, paddingBottom: 40 },
  modalTitle: { color: '#fff', fontSize: 20, fontWeight: 'bold', marginBottom: 16 },
  label: { color: COLORS.textMuted, fontSize: 13, marginBottom: 6, marginTop: 12 },
  input: { backgroundColor: COLORS.surfaceLight, color: '#fff', borderRadius: 10, paddingHorizontal: 14, paddingVertical: 12, fontSize: 15, borderWidth: 1, borderColor: COLORS.border },
  modalActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 12, marginTop: 20 },
  cancelBtn: { paddingVertical: 10, paddingHorizontal: 16, borderRadius: 10 },
  btn: { backgroundColor: COLORS.primary, borderRadius: 10, paddingVertical: 10, paddingHorizontal: 20, alignItems: 'center' },
  btnDisabled: { opacity: 0.6 },
  btnText: { color: '#fff', fontWeight: 'bold' },
});
