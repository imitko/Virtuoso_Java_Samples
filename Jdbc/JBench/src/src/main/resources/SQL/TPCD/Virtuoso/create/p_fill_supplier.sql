--  
--  This file is part of the OpenLink Software Virtuoso Open-Source (VOS)
--  project.
--  
--  Copyright (C) 1998-2021 OpenLink Software
--  
--  This project is free software; you can redistribute it and/or modify it
--  under the terms of the GNU General Public License as published by the
--  Free Software Foundation; only version 2 of the License, dated June 1991.
--  
--  This program is distributed in the hope that it will be useful, but
--  WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
--  General Public License for more details.
--  
--  You should have received a copy of the GNU General Public License along
--  with this program; if not, write to the Free Software Foundation, Inc.,
--  51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
--  
--  
create procedure tpcd..fill_supplier (in initial_suppkey integer, in NumRows integer) {
	
	declare _s_suppkey, _s_nationkey integer;
	declare _s_name, _s_address, _s_phone, _s_comment varchar;
	declare _s_acctbal numeric;
	
	_s_suppkey := initial_suppkey;
	
	while (_s_suppkey <= NumRows) {
		
		_s_name := concat('Supplier', sprintf('%d', _s_suppkey));
		_s_address := tpcd..randomString(25, 0, 1);
		_s_nationkey := tpcd..randomNumber(0, 24);
		_s_phone := tpcd..randomPhone(_s_nationkey);
		_s_acctbal := tpcd..randomNumeric(-999.99, 9999.99);
		_s_comment := tpcd..randomString(63, 0, 1);
		
		
		insert into tpcd..supplier (s_suppkey, s_name, s_address, s_nationkey, s_phone, s_acctbal, s_comment) values (_s_suppkey, _s_name, _s_address, _s_nationkey, _s_phone, _s_acctbal, _s_comment);
		_s_suppkey := _s_suppkey + 1;
	}
}
