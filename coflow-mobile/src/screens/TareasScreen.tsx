import React, { useCallback, useEffect, useState } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  ActivityIndicator, RefreshControl, Modal, TextInput, Alert, ScrollView,
} from 'react-native';
import { tareaService } from '../services/tareaService';
import { proyectoService } from '../services/proyectoService';
import { Tarea, Proyecto, EstadoTarea, TareaCreateRequest } from '../types/types';
import { COLORS, ESTADO_LABEL, ESTADO_COLOR, PRIORIDAD_COLOR, formatDate } from '../components/theme';
import { useAuth } from '../auth/AuthContext';

function Badge({ label, color }: { label: string; color: string }) {
  return (
    <View style={[styles.badge, { backgroundColor: color + '33', borderColor: color }]}>
      <Text style={[styles.badgeText, { color }]}>{label}</Text>
    </View>
  );
}

function TareaCard({ tarea, onRefresh, onPress }: { tarea: Tarea; onRefresh: () => void; onPress: () => void }) {
  const { user } = useAuth();
  const esAsignado = tarea.usuarioAsignado?.id === user?.usuarioId;

  const cambiarEstado = async (accion: 'ACEPTAR' | 'RECHAZAR') => {
    try {
      await tareaService.cambiarEstado(tarea.id, accion);
      onRefresh();
    } catch (e: any) { Alert.alert('Error', e.message); }
  };

  const completar = async () => {
    try {
      await tareaService.moverEstado(tarea.id, 'COMPLETADA');
      onRefresh();
    } catch (e: any) { Alert.alert('Error', e.message); }
  };

  return (
    <TouchableOpacity style={styles.card} onPress={onPress} activeOpacity={0.8}>
      <View style={styles.cardHeader}>
        <Text style={styles.cardTitle} numberOfLines={2}>{tarea.titulo}</Text>
        <Badge label={ESTADO_LABEL[tarea.estado]} color={ESTADO_COLOR[tarea.estado]} />
      </View>

      <View style={styles.cardMeta}>
        <Badge label={tarea.prioridad} color={PRIORIDAD_COLOR[tarea.prioridad]} />
        <Text style={styles.metaText}>📁 {tarea.proyectoTitulo}</Text>
      </View>

      <View style={styles.cardFooter}>
        <Text style={styles.metaText}>
          👤 {tarea.usuarioAsignado?.nombreCompleto ?? 'Sin asignar'}
        </Text>
        {tarea.fechaLimite && (
          <Text style={styles.metaText}>📅 {formatDate(tarea.fechaLimite)}</Text>
        )}
      </View>

      <View style={styles.cardStats}>
        <Text style={styles.statText}>💬 {tarea.totalComentarios}</Text>
        <Text style={styles.statText}>📎 {tarea.totalEntregables}</Text>
      </View>

      {esAsignado && tarea.estado === 'ASIGNADA' && (
        <View style={styles.actionRow}>
          <TouchableOpacity style={[styles.actionBtn, { backgroundColor: COLORS.success }]}
            onPress={() => cambiarEstado('ACEPTAR')}>
            <Text style={styles.actionBtnText}>✓ Aceptar</Text>
          </TouchableOpacity>
          <TouchableOpacity style={[styles.actionBtn, { backgroundColor: COLORS.danger }]}
            onPress={() => cambiarEstado('RECHAZAR')}>
            <Text style={styles.actionBtnText}>✗ Rechazar</Text>
          </TouchableOpacity>
        </View>
      )}

      {esAsignado && tarea.estado === 'EN_PROCESO' && (
        <TouchableOpacity style={[styles.actionBtn, { backgroundColor: COLORS.primary, marginTop: 8 }]}
          onPress={completar}>
          <Text style={styles.actionBtnText}>✓ Marcar completada</Text>
        </TouchableOpacity>
      )}
    </TouchableOpacity>
  );
}

export default function TareasScreen({ navigation }: any) {
  const { user } = useAuth();
  const [tareas, setTareas] = useState<Tarea[]>([]);
  const [proyectos, setProyectos] = useState<Proyecto[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [filtroEstado, setFiltroEstado] = useState<EstadoTarea | 'TODAS'>('TODAS');
  const [showModal, setShowModal] = useState(false);

  // Crear tarea form
  const [nuevaTitulo, setNuevaTitulo] = useState('');
  const [nuevaDescripcion, setNuevaDescripcion] = useState('');
  const [proyectoSeleccionado, setProyectoSeleccionado] = useState<number | null>(null);
  const [creando, setCreando] = useState(false);

  const cargar = useCallback(async () => {
    try {
      const [tareasRes, proyectosRes] = await Promise.all([
        tareaService.listar(),
        proyectoService.listar(),
      ]);
      setTareas(tareasRes.data);
      setProyectos(proyectosRes.data);
    } catch (e: any) {
      Alert.alert('Error', e.message);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { cargar(); }, [cargar]);

  const tareasFiltradas = filtroEstado === 'TODAS'
    ? tareas
    : tareas.filter(t => t.estado === filtroEstado);

  const crearTarea = async () => {
    if (!nuevaTitulo.trim() || !proyectoSeleccionado) {
      Alert.alert('Error', 'Título y proyecto son obligatorios');
      return;
    }
    setCreando(true);
    try {
      const dto: TareaCreateRequest = {
        proyectoId: proyectoSeleccionado,
        titulo: nuevaTitulo.trim(),
        descripcion: nuevaDescripcion.trim() || undefined,
      };
      await tareaService.crear(dto);
      setShowModal(false);
      setNuevaTitulo('');
      setNuevaDescripcion('');
      setProyectoSeleccionado(null);
      await cargar();
    } catch (e: any) {
      Alert.alert('Error', e.message);
    } finally {
      setCreando(false);
    }
  };

  const FILTROS: (EstadoTarea | 'TODAS')[] = ['TODAS', 'PENDIENTE', 'ASIGNADA', 'EN_PROCESO', 'EN_REVISION', 'COMPLETADA'];

  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color={COLORS.primary} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      {/* Filtros */}
      <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.filtros} contentContainerStyle={{ paddingHorizontal: 16, paddingVertical: 10 }}>
        {FILTROS.map(f => (
          <TouchableOpacity
            key={f}
            onPress={() => setFiltroEstado(f)}
            style={[styles.filtroBtn, filtroEstado === f && styles.filtroBtnActive]}
          >
            <Text style={[styles.filtroText, filtroEstado === f && styles.filtroTextActive]}>
              {f === 'TODAS' ? 'Todas' : ESTADO_LABEL[f as EstadoTarea]}
            </Text>
          </TouchableOpacity>
        ))}
      </ScrollView>

      {/* Lista */}
      <FlatList
        data={tareasFiltradas}
        keyExtractor={t => String(t.id)}
        renderItem={({ item }) => (
          <TareaCard
            tarea={item}
            onRefresh={cargar}
            onPress={() => navigation.navigate('DetalleTarea', { tarea: item, onRefresh: cargar })}
          />
        )}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); cargar(); }} tintColor={COLORS.primary} />}
        contentContainerStyle={{ padding: 16, paddingBottom: 100 }}
        ListEmptyComponent={<Text style={styles.empty}>No hay tareas en este estado</Text>}
      />

      {/* FAB */}
      {(user?.rol === 'ADMIN' || user?.rol === 'LIDER') && (
        <TouchableOpacity style={styles.fab} onPress={() => setShowModal(true)}>
          <Text style={styles.fabText}>+</Text>
        </TouchableOpacity>
      )}

      {/* Modal nueva tarea */}
      <Modal visible={showModal} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={styles.modalCard}>
            <Text style={styles.modalTitle}>Nueva tarea</Text>

            <Text style={styles.label}>Proyecto</Text>
            <ScrollView style={{ maxHeight: 120 }}>
              {proyectos.map(p => (
                <TouchableOpacity
                  key={p.id}
                  onPress={() => setProyectoSeleccionado(p.id)}
                  style={[styles.proyectoItem, proyectoSeleccionado === p.id && styles.proyectoItemSelected]}
                >
                  <Text style={{ color: proyectoSeleccionado === p.id ? COLORS.primary : COLORS.text }}>{p.titulo}</Text>
                </TouchableOpacity>
              ))}
            </ScrollView>

            <Text style={styles.label}>Título *</Text>
            <TextInput style={styles.input} placeholder="Nombre de la tarea" placeholderTextColor={COLORS.textMuted}
              value={nuevaTitulo} onChangeText={setNuevaTitulo} />

            <Text style={styles.label}>Descripción</Text>
            <TextInput style={[styles.input, { height: 80 }]} placeholder="Descripción opcional..."
              placeholderTextColor={COLORS.textMuted} value={nuevaDescripcion}
              onChangeText={setNuevaDescripcion} multiline />

            <View style={styles.modalActions}>
              <TouchableOpacity style={styles.cancelBtn} onPress={() => setShowModal(false)}>
                <Text style={{ color: COLORS.textMuted }}>Cancelar</Text>
              </TouchableOpacity>
              <TouchableOpacity style={[styles.btn, creando && styles.btnDisabled]} onPress={crearTarea} disabled={creando}>
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
  filtros: { backgroundColor: COLORS.surface, flexGrow: 0 },
  filtroBtn: { paddingHorizontal: 14, paddingVertical: 6, borderRadius: 20, marginRight: 8, backgroundColor: COLORS.surfaceLight },
  filtroBtnActive: { backgroundColor: COLORS.primary },
  filtroText: { color: COLORS.textMuted, fontSize: 13 },
  filtroTextActive: { color: '#fff', fontWeight: '600' },
  card: { backgroundColor: COLORS.surface, borderRadius: 14, padding: 16, marginBottom: 12, borderWidth: 1, borderColor: COLORS.border },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 10 },
  cardTitle: { flex: 1, color: '#fff', fontSize: 15, fontWeight: '600', marginRight: 10 },
  cardMeta: { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 8, flexWrap: 'wrap' },
  cardFooter: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 },
  cardStats: { flexDirection: 'row', gap: 16 },
  badge: { paddingHorizontal: 8, paddingVertical: 3, borderRadius: 20, borderWidth: 1 },
  badgeText: { fontSize: 11, fontWeight: '600' },
  metaText: { color: COLORS.textMuted, fontSize: 12 },
  statText: { color: COLORS.textMuted, fontSize: 12 },
  actionRow: { flexDirection: 'row', gap: 8, marginTop: 10 },
  actionBtn: { flex: 1, paddingVertical: 8, borderRadius: 8, alignItems: 'center' },
  actionBtnText: { color: '#fff', fontWeight: '600', fontSize: 13 },
  empty: { color: COLORS.textMuted, textAlign: 'center', marginTop: 40, fontSize: 15 },
  fab: {
    position: 'absolute', bottom: 24, right: 24,
    width: 56, height: 56, borderRadius: 28,
    backgroundColor: COLORS.primary, justifyContent: 'center', alignItems: 'center',
    elevation: 6, shadowColor: '#000', shadowOpacity: 0.3, shadowRadius: 8,
  },
  fabText: { color: '#fff', fontSize: 28, fontWeight: 'bold', lineHeight: 32 },
  modalOverlay: { flex: 1, backgroundColor: '#000a', justifyContent: 'flex-end' },
  modalCard: { backgroundColor: COLORS.surface, borderTopLeftRadius: 20, borderTopRightRadius: 20, padding: 24, paddingBottom: 40 },
  modalTitle: { color: '#fff', fontSize: 20, fontWeight: 'bold', marginBottom: 16 },
  label: { color: COLORS.textMuted, fontSize: 13, marginBottom: 6, marginTop: 12 },
  input: { backgroundColor: COLORS.surfaceLight, color: '#fff', borderRadius: 10, paddingHorizontal: 14, paddingVertical: 12, fontSize: 15, borderWidth: 1, borderColor: COLORS.border },
  proyectoItem: { padding: 10, borderRadius: 8, marginBottom: 4, backgroundColor: COLORS.surfaceLight },
  proyectoItemSelected: { borderWidth: 1, borderColor: COLORS.primary },
  modalActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 12, marginTop: 20 },
  cancelBtn: { paddingVertical: 10, paddingHorizontal: 16, borderRadius: 10 },
  btn: { backgroundColor: COLORS.primary, borderRadius: 10, paddingVertical: 10, paddingHorizontal: 20, alignItems: 'center' },
  btnDisabled: { opacity: 0.6 },
  btnText: { color: '#fff', fontWeight: 'bold' },
});
