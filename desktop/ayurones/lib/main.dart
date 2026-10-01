import 'package:flutter/material.dart';

void main() => runApp(const AyuronesDesktop());

class AyuronesDesktop extends StatefulWidget {
  const AyuronesDesktop({super.key});
  @override State<AyuronesDesktop> createState() => _AyuronesDesktopState();
}

class _AyuronesDesktopState extends State<AyuronesDesktop> {
  final Map<String, List<String>> chats = {
    'Сохранённые': [],
    'Ayurones Test Server': ['Система: тестовая среда готова.'],
    'Идеи': ['Добро пожаловать в Ayurones.'],
  };
  String selected = 'Сохранённые';
  final TextEditingController input = TextEditingController();
  int points = 250;
  int tab = 0;
  bool decoration = false;

  void send() {
    final text = input.text.trim();
    if (text.isEmpty) return;
    setState(() {
      chats[selected]!.add(text);
      input.clear();
      points += 5;
    });
  }

  void addChat() {
    final c = TextEditingController();
    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Новый чат'),
        content: TextField(controller: c, autofocus: true, decoration: const InputDecoration(hintText: 'Имя или @username')),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('Отмена')),
          FilledButton(onPressed: () {
            final name = c.text.trim().replaceFirst('@', '');
            if (name.isNotEmpty) setState(() { chats.putIfAbsent(name, () => []); selected = name; });
            Navigator.pop(context);
          }, child: const Text('Открыть')),
        ],
      ),
    );
  }

  @override Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Ayurones',
      theme: ThemeData.dark(useMaterial3: true, colorSchemeSeed: Colors.white),
      home: Scaffold(
        body: SafeArea(
          child: Row(
            children: [
              SizedBox(
                width: 300,
                child: Container(
                  decoration: const BoxDecoration(border: Border(right: BorderSide(color: Color(0xff333333)))),
                  child: Column(
                    children: [
                      Padding(
                        padding: const EdgeInsets.fromLTRB(20,18,12,14),
                        child: Row(children:[
                          const Expanded(child: Text('Ayurones',style:TextStyle(fontSize:26,fontWeight:FontWeight.w700))),
                          IconButton(onPressed:addChat,icon:const Icon(Icons.add))
                        ]),
                      ),
                      const Divider(height:1),
                      Expanded(child: ListView(
                        children: [
                          for (final name in chats.keys)
                            ListTile(
                              selected: name == selected,
                              title: Text(name),
                              subtitle: Text(chats[name]!.isEmpty ? 'Новый чат' : chats[name]!.last,maxLines:1,overflow:TextOverflow.ellipsis),
                              onTap: () => setState(() { selected = name; tab = 0; }),
                            ),
                        ],
                      )),
                      const Divider(height:1),
                      Padding(
                        padding: const EdgeInsets.all(8),
                        child: Row(children:[
                          Expanded(child: TextButton.icon(onPressed:()=>setState(()=>tab=0),icon:const Icon(Icons.chat_bubble_outline),label:const Text('Чаты'))),
                          Expanded(child: TextButton.icon(onPressed:()=>setState(()=>tab=1),icon:const Icon(Icons.person_outline),label:const Text('Профиль'))),
                        ]),
                      ),
                    ],
                  ),
                ),
              ),
              Expanded(child: tab == 1 ? buildProfile() : buildChat()),
            ],
          ),
        ),
      ),
    );
  }

  Widget buildChat() {
    final list = chats[selected]!;
    return Column(children:[
      Container(
        height:70,
        padding:const EdgeInsets.symmetric(horizontal:24),
        alignment:Alignment.centerLeft,
        decoration:const BoxDecoration(border:Border(bottom:BorderSide(color:Color(0xff333333)))),
        child:Text(selected,style:const TextStyle(fontSize:19,fontWeight:FontWeight.w700)),
      ),
      Expanded(child: ListView.builder(
        padding:const EdgeInsets.all(24),
        itemCount:list.length,
        itemBuilder:(c,i)=>Align(
          alignment:Alignment.centerRight,
          child:Container(
            margin:const EdgeInsets.only(bottom:9),
            padding:const EdgeInsets.symmetric(horizontal:16,vertical:11),
            decoration:BoxDecoration(color:const Color(0xff2a2a2a),borderRadius:BorderRadius.circular(16)),
            child:Text(list[i]),
          ),
        ),
      )),
      Padding(
        padding:const EdgeInsets.fromLTRB(18,8,18,18),
        child:Row(children:[
          Expanded(child:TextField(controller:input,maxLines:3,minLines:1,onSubmitted:(_)=>send(),decoration:InputDecoration(hintText:'Сообщение',filled:true,fillColor:const Color(0xff151515),border:OutlineInputBorder(borderRadius:BorderRadius.circular(20))))),
          const SizedBox(width:8),
          IconButton(onPressed:send,icon:const Icon(Icons.send)),
        ]),
      )
    ]);
  }

  Widget buildProfile() {
    return Center(
      child:SizedBox(width:420,child:Column(mainAxisAlignment:MainAxisAlignment.center,children:[
        Stack(alignment:Alignment.topRight,children:[
          Container(width:180,height:180,decoration:BoxDecoration(shape:BoxShape.circle,color:const Color(0xff262626),border:Border.all(color:Colors.white24,width:2)),child:const Icon(Icons.person,size:78)),
          if(decoration) const Padding(padding:EdgeInsets.all(2),child:Text('✦',style:TextStyle(fontSize:42))),
        ]),
        const SizedBox(height:18),
        const Text('Ayurones User',style:TextStyle(fontSize:24,fontWeight:FontWeight.w700)),
        Text('Баллы: $points',style:const TextStyle(color:Colors.white60)),
        const SizedBox(height:22),
        FilledButton(onPressed:()=>setState(()=>decoration=!decoration),child:Text(decoration?'Снять украшение':'Надеть ✦')),
        const SizedBox(height:8),
        OutlinedButton(onPressed:()=>setState(()=>points+=20),child:const Text('Ежедневная активность +20')),
      ])),
    );
  }

  @override void dispose(){input.dispose();super.dispose();}
}
