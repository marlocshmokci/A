import 'package:flutter/material.dart';

import '../services/url_launcher.dart' as ul;

class AyuronesServerScreen extends StatefulWidget {
  const AyuronesServerScreen({super.key});

  @override
  State<AyuronesServerScreen> createState() => _AyuronesServerScreenState();
}

class _AyuronesServerScreenState extends State<AyuronesServerScreen> {
  final _location = TextEditingController();
  final _traffic = TextEditingController();
  final _term = TextEditingController();
  final _notes = TextEditingController();

  String _protocol = 'VLESS';
  String _purpose = 'Personal';
  String _speed = 'Fast';

  bool get _ru => Localizations.localeOf(context).languageCode == 'ru';
  String _label(String en, String ru) => _ru ? ru : en;

  @override
  void dispose() {
    _location.dispose();
    _traffic.dispose();
    _term.dispose();
    _notes.dispose();
    super.dispose();
  }

  Future<void> _openTelegram() async {
    final request = [
      'Ayurones server request',
      'Location: ${_location.text.trim().isEmpty ? 'not specified' : _location.text.trim()}',
      'Protocol: ${_protocol}',
      'Speed: ${_speed}',
      'Traffic: ${_traffic.text.trim().isEmpty ? 'not specified' : _traffic.text.trim()}',
      'Term: ${_term.text.trim().isEmpty ? 'not specified' : _term.text.trim()}',
      'Purpose: ${_purpose}',
      'Details: ${_notes.text.trim().isEmpty ? 'none' : _notes.text.trim()}',
    ].join('\n');
    final url = 'https://t.me/eppere?text=${Uri.encodeQueryComponent(request)}';
    await ul.UrlLauncher.open(url);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(_label('Buy a server', 'Купить сервер'))),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(_label('Choose the server you need', 'Укажи, какой сервер тебе нужен'),
                      style: Theme.of(context).textTheme.titleLarge),
                  const SizedBox(height: 8),
                  Text(_label(
                    'Fill in the details. Ayurones will open a Telegram chat with the request already prepared.',
                    'Заполни подробности. Ayurones откроет Telegram с уже подготовленной заявкой.',
                  )),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _location,
            decoration: InputDecoration(
              labelText: _label('Location / country', 'Локация / страна'),
              hintText: _label('Germany, Netherlands, etc.', 'Германия, Нидерланды и т. д.'),
              prefixIcon: const Icon(Icons.public),
              border: const OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 12),
          DropdownButtonFormField<String>(
            initialValue: _protocol,
            decoration: InputDecoration(
              labelText: _label('Protocol', 'Протокол'),
              prefixIcon: const Icon(Icons.alt_route),
              border: const OutlineInputBorder(),
            ),
            items: const ['VLESS', 'VMess', 'Trojan', 'Shadowsocks', 'Hysteria2', 'WireGuard']
                .map((v) => DropdownMenuItem(value: v, child: Text(v))).toList(),
            onChanged: (v) => setState(() => _protocol = v ?? _protocol),
          ),
          const SizedBox(height: 12),
          DropdownButtonFormField<String>(
            initialValue: _speed,
            decoration: InputDecoration(
              labelText: _label('Performance', 'Производительность'),
              prefixIcon: const Icon(Icons.speed),
              border: const OutlineInputBorder(),
            ),
            items: [
              DropdownMenuItem(value: 'Balanced', child: Text(_label('Balanced', 'Сбалансированный'))),
              DropdownMenuItem(value: 'Fast', child: Text(_label('Fast', 'Быстрый'))),
              DropdownMenuItem(value: 'Maximum', child: Text(_label('Maximum', 'Максимальный'))),
            ],
            onChanged: (v) => setState(() => _speed = v ?? _speed),
          ),
          const SizedBox(height: 12),
          DropdownButtonFormField<String>(
            initialValue: _purpose,
            decoration: InputDecoration(
              labelText: _label('Purpose', 'Назначение'),
              prefixIcon: const Icon(Icons.flag_outlined),
              border: const OutlineInputBorder(),
            ),
            items: [
              DropdownMenuItem(value: 'Personal', child: Text(_label('Personal', 'Личный'))),
              DropdownMenuItem(value: 'Work', child: Text(_label('Work', 'Работа'))),
              DropdownMenuItem(value: 'Travel', child: Text(_label('Travel', 'Путешествия'))),
              DropdownMenuItem(value: 'Other', child: Text(_label('Other', 'Другое'))),
            ],
            onChanged: (v) => setState(() => _purpose = v ?? _purpose),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _traffic,
            keyboardType: TextInputType.number,
            decoration: InputDecoration(
              labelText: _label('Traffic per month', 'Трафик в месяц'),
              hintText: '100 GB',
              prefixIcon: const Icon(Icons.data_usage),
              border: const OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _term,
            decoration: InputDecoration(
              labelText: _label('Term', 'Срок'),
              hintText: _label('1 month / 3 months / etc.', '1 месяц / 3 месяца / и т. д.'),
              prefixIcon: const Icon(Icons.calendar_month),
              border: const OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _notes,
            maxLines: 5,
            decoration: InputDecoration(
              labelText: _label('Additional details', 'Дополнительные детали'),
              hintText: _label(
                'IPv6, several devices, special routing, preferred hostname, etc.',
                'IPv6, несколько устройств, особая маршрутизация, желаемое имя и т. п.',
              ),
              alignLabelWithHint: true,
              prefixIcon: const Padding(
                padding: EdgeInsets.only(bottom: 72),
                child: Icon(Icons.notes_outlined),
              ),
              border: const OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 20),
          FilledButton.icon(
            onPressed: _openTelegram,
            icon: const Icon(Icons.telegram),
            label: Text(_label('Open Telegram and request server', 'Открыть Telegram и заказать сервер')),
          ),
          const SizedBox(height: 8),
          Center(child: Text('t.me/eppere', style: Theme.of(context).textTheme.bodySmall)),
        ],
      ),
    );
  }
}
