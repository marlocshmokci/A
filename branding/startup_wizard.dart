import 'package:flutter/material.dart';

import '../../vpn/box_vpn_client.dart';
import 'home_dialogs.dart';

class StartupWizard {
  StartupWizard(this.context, this.vpn);

  final BuildContext context;
  final BoxVpnClient vpn;

  Future<void> run() async {
    if (!context.mounted) return;
    await maybeShowNotificationPermissionDialog(context);
    if (!context.mounted) return;
    await _showGuide();
  }

  Future<void> _showGuide() async {
    var page = 0;
    const pages = [
      (
        icon: Icons.waving_hand_outlined,
        title: 'Добро пожаловать в Ayurones',
        text: 'Ayurones — VPN-клиент. Здесь ты добавляешь сервер, выбираешь его и включаешь VPN. Если всё уже понятно, нажми «Пропустить».',
      ),
      (
        icon: Icons.add_circle_outline,
        title: '1. Добавь сервер',
        text: 'Открой список серверов и добавь конфигурацию. Можно использовать VLESS, VMess, Trojan, Shadowsocks, Hysteria2, WireGuard и другие поддерживаемые варианты.',
      ),
      (
        icon: Icons.play_circle_outline,
        title: '2. Подключись',
        text: 'Выбери сервер и нажми кнопку подключения. Android попросит разрешение на VPN — его нужно подтвердить.',
      ),
      (
        icon: Icons.tune,
        title: '3. Настройки',
        text: 'В настройках можно изменить режим работы и параметры подключения. Если не знаешь, что менять, оставь значения по умолчанию.',
      ),
      (
        icon: Icons.help_outline,
        title: '4. Нужна помощь?',
        text: 'Открой раздел руководства в About. Там есть полная инструкция на русском и английском. Этот мини-гид можно пропустить.',
      ),
    ];

    if (!context.mounted) return;
    await showDialog<void>(
      context: context,
      barrierDismissible: false,
      builder: (dialogContext) => StatefulBuilder(
        builder: (context, setState) {
          final p = pages[page];
          final last = page == pages.length - 1;
          return AlertDialog(
            titlePadding: const EdgeInsets.fromLTRB(24, 22, 24, 8),
            contentPadding: const EdgeInsets.fromLTRB(24, 8, 24, 8),
            actionsPadding: const EdgeInsets.fromLTRB(16, 0, 16, 14),
            title: Row(
              children: [
                Icon(p.icon),
                const SizedBox(width: 10),
                Expanded(child: Text(p.title)),
              ],
            ),
            content: Text(p.text, style: const TextStyle(height: 1.45)),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(dialogContext).pop(),
                child: const Text('Пропустить'),
              ),
              FilledButton(
                onPressed: () {
                  if (last) {
                    Navigator.of(dialogContext).pop();
                  } else {
                    setState(() => page++);
                  }
                },
                child: Text(last ? 'Понятно' : 'Далее'),
              ),
            ],
          );
        },
      ),
    );
  }
}
