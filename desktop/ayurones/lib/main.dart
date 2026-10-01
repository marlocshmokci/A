import 'package:flutter/material.dart';

void main() => runApp(const AyuronesDesktop());

class AyuronesDesktop extends StatelessWidget {
  const AyuronesDesktop({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Ayurones',
      theme: ThemeData.dark(useMaterial3: true),
      home: Scaffold(
        appBar: AppBar(title: const Text('Ayurones v0.2.0')),
        body: const Center(
          child: Text(
            'Ayurones Desktop\n\nPortable Windows client shell',
            textAlign: TextAlign.center,
          ),
        ),
      ),
    );
  }
}
