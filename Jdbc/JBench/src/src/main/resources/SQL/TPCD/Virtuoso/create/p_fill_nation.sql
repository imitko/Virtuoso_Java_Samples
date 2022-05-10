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
create procedure tpcd..fill_nation(in n integer) {
	
	declare _n_nationkey, _n_regionkey integer;
	declare _n_name, _n_comment varchar;
	
	declare namearray, regionarray integer;
	
	namearray := vector ('ALGERIA', 'ARGENTINA','BRAZIL','CANADA','EGYPT','ETHIOPIA','FRANCE','GERMANY','INDIA','INDONESIA','IRAN','IRAQ','JAPAN','JORDAN','KENYA','MOROCCO','MOZAMBIQUE','PERU','CHINA','ROMANIA','SAUDI ARABIA','VIETNAM','RUSSIA','UNITED KINGDOM','UNITED STATES');
	regionarray := vector(0, 1, 1, 1, 4, 0, 3, 3, 2, 2, 4, 4, 2, 4, 0, 0, 0, 1, 2, 3, 4, 2, 3, 3, 1);
				 
	_n_nationkey := 0;
	while (_n_nationkey <= 24) {
		
		_n_name := aref(namearray, _n_nationkey);
		_n_regionkey := aref(regionarray, _n_nationkey);
		_n_comment := tpcd..randomString(95, 0, 1);
					
		insert into tpcd..nation (n_nationkey, n_name, n_regionkey, n_comment) values (_n_nationkey, _n_name, _n_regionkey, _n_comment);
		
		_n_nationkey := _n_nationkey + 1;
	}
}
