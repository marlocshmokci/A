import 'package:flutter/material.dart';

import '../services/project_links.dart';
import '../services/url_launcher.dart' as ul;
import '../services/version_info.dart';
import '../vpn/box_vpn_client.dart';
import '../widgets/safe_bottom.dart';

class AboutScreen extends StatelessWidget {
  const AboutScreen({super.key, this.openDonate = false});

  final bool openDonate;

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Scaffold(
      appBar: AppBar(title: const Text('Ayurones')),
      body: ListView(
        padding: const EdgeInsets.all(16).withSafeBottom(context),
        children: [
          Center(
            child: Column(
              children: [
                ClipRRect(
                  borderRadius: BorderRadius.circular(18),
                  child: Image.asset('assets/icons/app_icon.png', width: 88, height: 88),
                ),
                const SizedBox(height: 10),
                Text('Ayurones',
                    style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w700)),
                const SizedBox(height: 4),
                Text('v${VersionInfo.I.version}', style: TextStyle(color: cs.onSurfaceVariant)),
              ],
            ),
          ),
          const SizedBox(height: 18),
          Card(
            child: Column(
              children: [
                ListTile(
                  leading: const Icon(Icons.menu_book_outlined),
                  title: const Text('English guide'),
                  subtitle: const Text('Complete Ayurones interface walkthrough'),
                  trailing: const Icon(Icons.open_in_new, size: 18),
                  onTap: () => ul.UrlLauncher.open(ProjectLinks.guideEn),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.menu_book_outlined),
                  title: const Text('Русское руководство'),
                  subtitle: const Text('Полная инструкция по Ayurones'),
                  trailing: const Icon(Icons.open_in_new, size: 18),
                  onTap: () => ul.UrlLauncher.open(ProjectLinks.guideRu),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.code),
                  title: const Text('Ayurones on GitHub'),
                  subtitle: const Text(ProjectLinks.repo),
                  trailing: const Icon(Icons.open_in_new, size: 18),
                  onTap: () => ul.UrlLauncher.open(ProjectLinks.repo),
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),
          Card(
            child: ListTile(
              leading: const Icon(Icons.architecture),
              title: const Text('VPN engine'),
              subtitle: FutureBuilder<String>(
                future: BoxVpnClient.I.getCoreVersion(),
                builder: (_, snap) => Text(
                  (snap.data ?? '').isEmpty ? 'sing-box core' : 'sing-box ${snap.data}',
                ),
              ),
            ),
          ),
          const SizedBox(height: 12),
          Card(
            child: Column(
              children: [
                ListTile(
                  leading: const Icon(Icons.download_outlined),
                  title: const Text('GitHub Releases'),
                  subtitle: const Text('APK builds and release notes'),
                  onTap: () => ul.UrlLauncher.open(ProjectLinks.releases),
                ),
                ListTile(
                  leading: const Icon(Icons.telegram),
                  title: const Text('Server purchase'),
                  subtitle: const Text('t.me/eppere'),
                  onTap: () => ul.UrlLauncher.open('https://t.me/eppere'),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
