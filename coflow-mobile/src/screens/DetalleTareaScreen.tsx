import React, { useCallback, useEffect, useState } from 'react';
import {
  View, Text, StyleSheet, ScrollView, TouchableOpacity,
  TextInput, ActivityIndicator, Alert, KeyboardAvoidingView, Platform,
} from 'react-native';
import { tareaService, comentarioService } from '../services/tareaService';
import { Tarea, Comentario } from '../types/types';
import { COLORS, ESTADO_LABEL, ESTADO_COLOR, PRIORIDAD_COLOR, formatDate } from '../components/theme';
import { useAuth } from '../auth/AuthContext';

function Badge({ label, color }: { label: string; color: string }) {
  return (
    <View style={[styles.badge, { backgroundColor: color + '33', borderColor: color }]}>
      <Text style={[styles.badgeText, { color }]}>{label}</Text>
    </View>
  );
}

export default function DetalleTareaScreen({ route, navigation }: any) {
  const { user } = useAuth();
  const { tarea: tareaInicial } = route.params;
  const [tarea, setTarea] = useState<Tarea>(tareaInicial);
  const [comentarios, setComentarios] = useState<Comentario[]>([]);
  const [texto, setTexto] = useState('');
  const [loadingComentarios, setLoadingComentarios] = useState(false);
  const [enviando, setEnviando] = useState(false);

  const cargarTarea = useCallback(async () => {
    try {
      const res = await tareaService.obtener(tarea.id);
      setTarea(res.data);
    } catch { /* ignorar */ }
  }, [tarea.id]);

  const cargarComentarios = useCallback(async () => {
    setLoadingComentarios(true);
    try {
      const res = await comentarioService.listarPorTarea(tarea.id);
      setComentarios(res.data);
    } catch { /* ignorar */ } finally {
      setLoadingComentarios(false);
    }
  }, [tarea.id]);

  useEffect(() => {
    cargarComentarios();
  }, [cargarComentarios]);

  const enviarComentario = async () => {
    if (!texto.trim()) return;
    setEnviando(true);
    try {
      await comentarioService.crear(tarea.id, texto.trim());
      setTexto('');
      await cargarComentarios();
    } catch (e: any) {
      Alert.alert('Error', e.message);
    } finally {
      setEnviando(false);
    }
  };

  const eliminarComentario = async (id: number) => {
    Alert.alert('Eliminar', '¿Seguro que quieres eliminar este comentario?', [
      { text: 'Cancelar', style: 'cancel' },
      {
        text: 'Eliminar', style: 'destructive', onPress: async () => {
          try {
            await comentarioService.eliminar(id);
            await cargarComentarios();
          } catch (e: any) { Alert.alert('Error', e.message); }
        }
      }
    ]);
  };

  const cambiarEstado = async (accion: 'ACEPTAR' | 'RECHAZAR') => {
    try {
      await tareaService.cambiarEstado(tarea.id, accion);
      await cargarTarea();
    } catch (e: any) { Alert.alert('Error', e.message); }
  };

  const completar = async () => {
    try {
      await tareaService.moverEstado(tarea.id, 'COMPLETADA');
      await cargarTarea();
    } catch (e: any) { Alert.alert('Error', e.message); }
  };

  const esAsignado = tarea.usuarioAsignado?.id === user?.usuarioId;

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      keyboardVerticalOffset={90}
    >
      <ScrollView contentContainerStyle={styles.scroll}>
        {/* Cabecera */}
        <View style={styles.card}>
          <Text style={styles.titulo}>{tarea.titulo}</Text>

          <View style={styles.row}>
            <Badge label={ESTADO_LABEL[tarea.estado]} color={ESTADO_COLOR[tarea.estado]} />
            <Badge label={tarea.prioridad} color={PRIORIDAD_COLOR[tarea.prioridad]} />
          </View>

          {tarea.descripcion ? (
            <Text style={styles.descripcion}>{tarea.descripcion}</Text>
          ) : (
            <Text style={styles.noDesc}>Sin descripción</Text>
          )}

          <View style={styles.infoGrid}>
            <View style={styles.infoItem}>
              <Text style={styles.infoLabel}>Proyecto</Text>
              <Text style={styles.infoValue}>{tarea.proyectoTitulo}</Text>
            </View>
            <View style={styles.infoItem}>
              <Text style={styles.infoLabel}>Asignado a</Text>
              <Text style={styles.infoValue}>{tarea.usuarioAsignado?.nombreCompleto ?? '—'}</Text>
            </View>
            <View style={styles.infoItem}>
              <Text style={styles.infoLabel}>Creado por</Text>
              <Text style={styles.infoValue}>{tarea.creador?.nombreCompleto ?? '—'}</Text>
            </View>
            <View style={styles.infoItem}>
              <Text style={styles.infoLabel}>Fecha límite</Text>
              <Text style={styles.infoValue}>{formatDate(tarea.fechaLimite)}</Text>
            </View>
            <View style={styles.infoItem}>
              <Text style={styles.infoLabel}>Tiempo estimado</Text>
              <Text style={styles.infoValue}>{tarea.tiempoEstimado ? `${tarea.tiempoEstimado}h` : '—'}</Text>
            </View>
            <View style={styles.infoItem}>
              <Text style={styles.infoLabel}>Requiere revisión</Text>
              <Text style={styles.infoValue}>{tarea.requiereRevision ? 'Sí' : 'No'}</Text>
            </View>
          </View>

          {/* Acciones */}
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
            <TouchableOpacity style={[styles.actionBtn, { backgroundColor: COLORS.primary, marginTop: 12 }]}
              onPress={completar}>
              <Text style={styles.actionBtnText}>✓ Marcar completada</Text>
            </TouchableOpacity>
          )}
        </View>

        {/* Comentarios */}
        <View style={styles.card}>
          <Text style={styles.sectionTitle}>Comentarios ({comentarios.length})</Text>

          {loadingComentarios && <ActivityIndicator color={COLORS.primary} style={{ marginVertical: 16 }} />}

          {!loadingComentarios && comentarios.length === 0 && (
            <Text style={styles.emptyText}>Sin comentarios todavía. ¡Sé el primero!</Text>
          )}

          {comentarios.map(c => (
            <View key={c.id} style={styles.comentario}>
              <View style={styles.comentarioHeader}>
                <Text style={styles.comentarioAutor}>{c.autorNombre}</Text>
                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                  <Text style={styles.comentarioFecha}>
                    {new Date(c.fechaCreacion).toLocaleDateString('es-ES', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })}
                  </Text>
                  {c.autorId === user?.usuarioId && (
                    <TouchableOpacity onPress={() => eliminarComentario(c.id)}>
                      <Text style={{ color: COLORS.danger, fontSize: 13 }}>🗑</Text>
                    </TouchableOpacity>
                  )}
                </View>
              </View>
              <Text style={styles.comentarioTexto}>{c.contenido}</Text>
            </View>
          ))}
        </View>
      </ScrollView>

      {/* Input comentario */}
      <View style={styles.inputBar}>
        <TextInput
          style={styles.input}
          placeholder="Escribe un comentario…"
          placeholderTextColor={COLORS.textMuted}
          value={texto}
          onChangeText={setTexto}
          maxLength={500}
          editable={!enviando}
        />
        <TouchableOpacity
          style={[styles.sendBtn, (!texto.trim() || enviando) && styles.sendBtnDisabled]}
          onPress={enviarComentario}
          disabled={!texto.trim() || enviando}
        >
          {enviando
            ? <ActivityIndicator color="#fff" size="small" />
            : <Text style={styles.sendBtnText}>➤</Text>
          }
        </TouchableOpacity>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.deepBlue },
  scroll: { padding: 16, paddingBottom: 24 },
  card: { backgroundColor: COLORS.surface, borderRadius: 14, padding: 16, marginBottom: 16, borderWidth: 1, borderColor: COLORS.border },
  titulo: { color: '#fff', fontSize: 20, fontWeight: 'bold', marginBottom: 12 },
  row: { flexDirection: 'row', gap: 8, marginBottom: 12, flexWrap: 'wrap' },
  badge: { paddingHorizontal: 8, paddingVertical: 3, borderRadius: 20, borderWidth: 1 },
  badgeText: { fontSize: 11, fontWeight: '600' },
  descripcion: { color: COLORS.textMuted, fontSize: 14, lineHeight: 20, marginBottom: 16 },
  noDesc: { color: COLORS.border, fontSize: 13, fontStyle: 'italic', marginBottom: 16 },
  infoGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 12 },
  infoItem: { width: '47%' },
  infoLabel: { color: COLORS.textMuted, fontSize: 11, textTransform: 'uppercase', letterSpacing: 0.5 },
  infoValue: { color: '#fff', fontSize: 14, fontWeight: '500', marginTop: 2 },
  actionRow: { flexDirection: 'row', gap: 8, marginTop: 16 },
  actionBtn: { flex: 1, paddingVertical: 10, borderRadius: 10, alignItems: 'center' },
  actionBtnText: { color: '#fff', fontWeight: '600' },
  sectionTitle: { color: '#fff', fontSize: 16, fontWeight: 'bold', marginBottom: 12 },
  emptyText: { color: COLORS.textMuted, fontSize: 13, textAlign: 'center', marginVertical: 12 },
  comentario: { backgroundColor: COLORS.surfaceLight, borderRadius: 10, padding: 12, marginBottom: 8 },
  comentarioHeader: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 4 },
  comentarioAutor: { color: COLORS.primary, fontSize: 13, fontWeight: '600' },
  comentarioFecha: { color: COLORS.textMuted, fontSize: 11 },
  comentarioTexto: { color: COLORS.text, fontSize: 14 },
  inputBar: { flexDirection: 'row', padding: 12, backgroundColor: COLORS.surface, borderTopWidth: 1, borderTopColor: COLORS.border, gap: 8 },
  input: { flex: 1, backgroundColor: COLORS.surfaceLight, color: '#fff', borderRadius: 10, paddingHorizontal: 14, paddingVertical: 10, fontSize: 14, borderWidth: 1, borderColor: COLORS.border },
  sendBtn: { backgroundColor: COLORS.primary, borderRadius: 10, width: 44, justifyContent: 'center', alignItems: 'center' },
  sendBtnDisabled: { opacity: 0.4 },
  sendBtnText: { color: '#fff', fontSize: 18 },
});
