import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:yamus/api/api.dart';
import 'package:yamus/providers/user_provider.dart';
import 'package:yamus/widgets/page_layout.dart';

class ProfilePage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final api = context.read<Api>();
    final user = context.watch<UserProvider>();

    return PageLayout(
      title: 'Profile',
      actions: [
        IconButton(
          icon: Icon(Icons.logout),
          onPressed: () => user.logout()
        ),
      ],
      child: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          ListTile(
            leading: CircleAvatar(
              
            ),
            title: Text(user.accountStatus.account.displayName),
            subtitle: Text(user.accountStatus.account.login),
          )
        ],
      ),
    );
  }
}